package com.yedam.hr.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.HrHistory;
import com.yedam.hr.domain.Employee;
import com.yedam.hr.dto.HrHistoryDTO;
import com.yedam.hr.repository.EmployeeRepository;
import com.yedam.hr.repository.HrHistoryRepository;
import com.yedam.hr.service.HrHistorySerivce;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HrHistorySerivceImpl implements HrHistorySerivce {

    private final HrHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;

    private static String sv(String s){ return s == null ? "" : s.trim(); }

    private Map<String, String> buildNameMap(Collection<String> codes){
        if (codes == null || codes.isEmpty()) return Collections.emptyMap();
        // findAllById 한 번으로 이름 벌크 로딩 → N+1 제거
        return employeeRepository.findAllById(codes)
                .stream()
                .collect(Collectors.toMap(Employee::getEmpCode, Employee::getName, (a,b)->a));
    }

    private HrHistoryDTO toDto(HrHistory h, Map<String,String> nameMap){
        String empName = nameMap.getOrDefault(sv(h.getEmpCode()), "(없음)");
        String mgrName = nameMap.getOrDefault(sv(h.getManager()),  "(없음)");
        return new HrHistoryDTO(
                h.getCompanyCode(),
                h.getEventType(),
                h.getEventDetail(),
                mgrName,
                h.getCreatedAt(),
                h.getEmpCode(),
                empName
        );
    }

    @Override
    public List<HrHistoryDTO> findByCompanyCode(String companyCode) {
        // 1) 전량 조회 (현행 유지)
        List<HrHistory> list = historyRepository.findByCompanyCodeOrderByCreatedAtDesc(companyCode);

        // 2) 필요한 사번(대상/담당자) 수집
        Set<String> codes = new HashSet<>();
        for (HrHistory h : list) {
            if (h.getEmpCode() != null) codes.add(h.getEmpCode().trim());
            if (h.getManager()  != null) codes.add(h.getManager().trim());
        }

        // 3) 벌크 로딩으로 이름 맵 구성
        Map<String,String> nameMap = buildNameMap(codes);

        // 4) DTO 변환 (연관 LAZY 접근 금지)
        return list.stream().map(h -> toDto(h, nameMap)).toList();
    }
}
