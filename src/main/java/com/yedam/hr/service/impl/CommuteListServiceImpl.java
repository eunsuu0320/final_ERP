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
        return commuteListRepository.findByCompanyCodeOrderByComIdDesc(companyCode);
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

        // (A) offTime의 '해당 일자' 계산
        ZoneId KST = ZoneId.of("Asia/Seoul");
        LocalDate offLocalDate = LocalDateTime.ofInstant(offTime.toInstant(), KST).toLocalDate();
        LocalDateTime dayStart = offLocalDate.atStartOfDay();
        LocalDateTime dayEnd   = offLocalDate.atTime(23, 59, 59);
        Date dayStartDate = Date.from(dayStart.atZone(KST).toInstant());
        Date dayEndDate   = Date.from(dayEnd.atZone(KST).toInstant());

        // (B) 그 날짜의 출근(onTime) 최신 1건 조회
        CommuteList commute = commuteListRepository
                .findLatestOnTimeRownum(companyCode, empCode, dayStartDate, dayEndDate);

        // ✅ 출근 기록 자체가 없으면: -1 반환
        if (commute == null || commute.getOnTime() == null) {
            return -1;
        }

        // ✅ 이미 퇴근 처리된 건이면: -2 반환 (원하면 메시지 분리)
        if (commute.getOffTime() != null) {
            return -2;
        }

        // (C) 퇴근 시간 반영
        commute.setOffTime(offTime);

        // ===== 근무시간/연장/야간/휴일 계산 =====
        LocalDateTime on  = LocalDateTime.ofInstant(commute.getOnTime().toInstant(), KST);
        LocalDateTime off = LocalDateTime.ofInstant(offTime.toInstant(), KST);

        // 비정상(off <= on) 방어
        if (!off.isAfter(on)) {
            commute.setWorkTime(0.0);
            commute.setOtTime(0.0);
            commute.setNightTime(0.0);
            commute.setHolidayTime(0.0);
            commuteListRepository.save(commute);
            return -3; // 시간 역전 케이스
        }

        long totalMinutes = Duration.between(on, off).toMinutes();

        // 휴게(4h↑ 30분, 8h↑ 60분)
        int breakMinutes = 0;
        if (totalMinutes >= 8 * 60)      breakMinutes = 60;
        else if (totalMinutes >= 4 * 60) breakMinutes = 30;

        long effectiveMinutes = Math.max(0, totalMinutes - breakMinutes);

        // 휴일/특근 여부
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

        long overtimeMinutes = (!holidayFlag && effectiveMinutes > 480)
            ? (effectiveMinutes - 480) : 0L;

        long holidayMinutes = holidayFlag ? effectiveMinutes : 0L;

        // 야간 22:00~06:00
        long nightMinutes = 0L;
        LocalDate startDate = on.toLocalDate();
        LocalDate endDate   = off.toLocalDate();
        for (LocalDate d = startDate.minusDays(1); !d.isAfter(endDate.plusDays(1)); d = d.plusDays(1)) {
            LocalDateTime n1Start = d.atTime(22, 0);
            LocalDateTime n1End   = d.plusDays(1).atTime(0, 0);
            LocalDateTime s1 = on.isAfter(n1Start) ? on : n1Start;
            LocalDateTime e1 = off.isBefore(n1End) ? off : n1End;
            if (e1.isAfter(s1)) nightMinutes += ChronoUnit.MINUTES.between(s1, e1);

            LocalDateTime n2Start = d.plusDays(1).atTime(0, 0);
            LocalDateTime n2End   = d.plusDays(1).atTime(6, 0);
            LocalDateTime s2 = on.isAfter(n2Start) ? on : n2Start;
            LocalDateTime e2 = off.isBefore(n2End) ? off : n2End;
            if (e2.isAfter(s2)) nightMinutes += ChronoUnit.MINUTES.between(s2, e2);
        }

        double workHours    = Math.round((effectiveMinutes / 60.0)      * 100.0) / 100.0;
        double nightHours   = Math.round((nightMinutes    / 60.0)       * 100.0) / 100.0;
        double overtimeHrs  = Math.round((overtimeMinutes / 60.0)       * 100.0) / 100.0;
        double holidayHours = Math.round((holidayMinutes  / 60.0)       * 100.0) / 100.0;

        if (holidayFlag) {
            commute.setWorkTime(0.0);
            commute.setHolidayTime(holidayHours);
            commute.setOtTime(0.0);
        } else {
            commute.setWorkTime(workHours);
            commute.setHolidayTime(0.0);
            commute.setOtTime(overtimeHrs);
        }
        commute.setNightTime(nightHours);

        commuteListRepository.save(commute);

        return 1; // 성공
    }

}
