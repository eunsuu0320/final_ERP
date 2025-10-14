package com.yedam.hr.service.impl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.CommuteList;
import com.yedam.hr.repository.CommuteListRepository;
import com.yedam.hr.service.CommuteListService;

import jakarta.transaction.Transactional;

@Service
public class CommuteListServiceImpl implements CommuteListService {

    @Autowired
    CommuteListRepository commuteListRepository;

    @Override
    public CommuteList insertCommute(CommuteList commuteList) {
        return commuteListRepository.save(commuteList);
    }

    @Override
    public List<CommuteList> getCommuteLists(String companyCode) {
        return commuteListRepository.findByCompanyCode(companyCode);
    }

    /**
     * 퇴근 처리:
     * - offTime 업데이트
     * - 근무/연장/야간/휴일(특근) 시간 산정 후 저장
     *   workTime, otTime, nightTime, holidayTime  ← 모두 "시간(Double)" 단위
     */
    @Override
    @Transactional
    public int punchOutByDate(String companyCode, String empCode, Date offTime) {

        // 0) 퇴근 시간 반영
        int updated = commuteListRepository.punchOutByDate(companyCode, empCode, offTime);
        if (updated == 0) return 0;

        // 1) offTime의 해당 일자 범위에서 onTime 최신 1건 조회
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate offLocalDate = LocalDateTime.ofInstant(offTime.toInstant(), KST).toLocalDate();
        LocalDateTime dayStart = offLocalDate.atStartOfDay();
        LocalDateTime dayEnd   = offLocalDate.atTime(23, 59, 59);
        Date dayStartDate = Date.from(dayStart.atZone(KST).toInstant());
        Date dayEndDate   = Date.from(dayEnd.atZone(KST).toInstant());

        CommuteList commute = commuteListRepository
        	    .findLatestOnTimeRownum(companyCode, empCode, dayStartDate, dayEndDate);

        if (commute == null || commute.getOnTime() == null || commute.getOffTime() == null) {
            return updated; // 안전 종료
        }

        // 2) 근무 구간
        LocalDateTime on  = LocalDateTime.ofInstant(commute.getOnTime().toInstant(), KST);
        LocalDateTime off = LocalDateTime.ofInstant(commute.getOffTime().toInstant(), KST);

        // 2-1) 비정상 구간 방어 (off <= on)
        if (!off.isAfter(on)) {
            commute.setWorkTime(0.0);
            commute.setOtTime(0.0);
            commute.setNightTime(0.0);
            commute.setHolidayTime(0.0);
            commuteListRepository.save(commute);
            return updated;
        }

        // 3) 총 근무분(휴게 전)
        long totalMinutes = Duration.between(on, off).toMinutes();

        // 4) 휴게 공제 (4h↑ 30분, 8h↑ 60분)
        int breakMinutes = 0;
        if (totalMinutes >= 8 * 60) {
            breakMinutes = 60;
        } else if (totalMinutes >= 4 * 60) {
            breakMinutes = 30;
        }

        // 5) 유효 근무분
        long effectiveMinutes = Math.max(0, totalMinutes - breakMinutes);

        // 6) 휴일/특근 여부 (토·일, comIs/메모)
        boolean weekend = (on.getDayOfWeek().getValue() == 6) || (on.getDayOfWeek().getValue() == 7);
        String comIs = commute.getComIs() == null ? "" : commute.getComIs();
        String note  = commute.getNote()  == null ? "" : commute.getNote();

        boolean holidayFlag =
               weekend
            || "H".equalsIgnoreCase(comIs)
            || comIs.toUpperCase().contains("HOLIDAY")
            || comIs.contains("특근")
            || note.contains("휴일")
            || note.toUpperCase().contains("HOLIDAY")
            || note.contains("특근");

        // 7) 연장(분): 평일·비휴일에서만 8시간(480분) 초과분
        long overtimeMinutes = (!holidayFlag && effectiveMinutes > 480)
            ? (effectiveMinutes - 480) : 0L;

        // 8) 휴일/특근 근무(분): 휴일이면 유효 근무 전부
        long holidayMinutes = holidayFlag ? effectiveMinutes : 0L;

        // 9) 야간(분): 22:00~06:00 겹침 합산
        long nightMinutes = 0L;
        LocalDate startDate = on.toLocalDate();
        LocalDate endDate   = off.toLocalDate();

        for (LocalDate d = startDate.minusDays(1); !d.isAfter(endDate.plusDays(1)); d = d.plusDays(1)) {
            // [d 22:00 ~ d+1 00:00]
            LocalDateTime n1Start = d.atTime(22, 0);
            LocalDateTime n1End   = d.plusDays(1).atTime(0, 0);
            LocalDateTime s1 = on.isAfter(n1Start) ? on : n1Start;
            LocalDateTime e1 = off.isBefore(n1End) ? off : n1End;
            if (e1.isAfter(s1)) nightMinutes += ChronoUnit.MINUTES.between(s1, e1);

            // [d+1 00:00 ~ d+1 06:00]
            LocalDateTime n2Start = d.plusDays(1).atTime(0, 0);
            LocalDateTime n2End   = d.plusDays(1).atTime(6, 0);
            LocalDateTime s2 = on.isAfter(n2Start) ? on : n2Start;
            LocalDateTime e2 = off.isBefore(n2End) ? off : n2End;
            if (e2.isAfter(s2)) nightMinutes += ChronoUnit.MINUTES.between(s2, e2);
        }

        // 10) 분 → 시간(Double) (소수 둘째 자리 반올림)
        double workHours    = Math.round((effectiveMinutes / 60.0)      * 100.0) / 100.0;
        double nightHours   = Math.round((nightMinutes    / 60.0)       * 100.0) / 100.0;
        double overtimeHrs  = Math.round((overtimeMinutes / 60.0)       * 100.0) / 100.0;
        double holidayHours = Math.round((holidayMinutes  / 60.0)       * 100.0) / 100.0;

        // 11) 저장값 반영
        if (holidayFlag) {
            // 휴일이면 workTime=0, holidayTime=유효근무
            commute.setWorkTime(0.0);
            commute.setHolidayTime(holidayHours);
            commute.setOtTime(0.0);          // 규정에 따라 휴일연장 분리 필요 시 변경
        } else {
            commute.setWorkTime(workHours);
            commute.setHolidayTime(0.0);
            commute.setOtTime(overtimeHrs);
        }
        commute.setNightTime(nightHours);

        // 12) 저장
        commuteListRepository.save(commute);

        return updated;
    }
}
