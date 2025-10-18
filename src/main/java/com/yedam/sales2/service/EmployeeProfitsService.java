package com.yedam.sales2.service;

import java.util.List;
import java.util.Map;

public interface EmployeeProfitsService {
	
	  // 사원별 판매 요약 데이터 조회
    List<Map<String, Object>> getEmployeeSummary(
        String companyCode,
        Integer year,
        Integer quarter,
        String keyword
    );
    
    // 사원관리 모달
    List<Map<String, Object>> getEmpPartners(
            String companyCode, String empCode, Integer year, Integer quarter, String keyword
        );
}