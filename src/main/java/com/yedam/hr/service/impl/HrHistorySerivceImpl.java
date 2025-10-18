package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yedam.hr.domain.HrHistory;
import com.yedam.hr.dto.HrHistoryDTO;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.HrHistoryRepository;
import com.yedam.hr.service.HrHistorySerivce;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class HrHistorySerivceImpl implements HrHistorySerivce {

    private final HrHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;

    @Override
    public List<HrHistoryDTO> findByCompanyCode(String companyCode) {
        List<HrHistory> list = historyRepository.findByCompanyCodeOrderByCreatedAtDesc(companyCode);

        // ★ 문제 요약 로그 (무엇이 null/미존재인지 바로 확인)
        for (HrHistory h : list) {
            String manager = h.getManager();
            boolean managerBlank = (manager == null) || manager.trim().isEmpty();

            if (managerBlank) {
                log.warn("[HIST-{}] manager가 비어있음 company={}, empCode={}",
                        h.getHistoryId(), h.getCompanyCode(), h.getEmpCode());
            } else {
                String key = manager.trim();
                boolean exists = employeeRepository.findById(key).isPresent();
                if (!exists) {
                    log.warn("[HIST-{}] manager 사번 미존재 company={}, manager='{}', trimmed='{}'",
                            h.getHistoryId(), h.getCompanyCode(), manager, key);
                }
            }

            // 참고: 레포가 INNER JOIN이면 employee는 거의 항상 존재
            if (h.getEmployee() == null) {
                log.warn("[HIST-{}] employee(대상 사원) 누락 company={}, empCode={}",
                        h.getHistoryId(), h.getCompanyCode(), h.getEmpCode());
            }
        }

        // ↓ 임시: 예외 대신 값 내려보며 로그 확인(문제 파악용)
        return list.stream().map(h -> {
            String managerCode = (h.getManager() == null ? "" : h.getManager().trim());
            String managerName = employeeRepository.findById(managerCode)
                    .map(emp -> emp.getName())
                    .orElse("(없음)"); // ← 일단 로그만 보려고 임시
            String empName = (h.getEmployee() == null) ? "(없음)" : h.getEmployee().getName();

            return new HrHistoryDTO(
                h.getCompanyCode(), h.getEventType(), h.getEventDetail(),
                managerName, h.getCreatedAt(), h.getEmpCode(), empName
            );
        }).toList();
    }
}
