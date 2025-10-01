package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.Allowance;

public interface AllowanceService {

	// 수당 조회
	List<Allowance> findByCompanyCode(String companyCode);

    // 여러 건 및 단 건 저장
    List<Allowance> saveAllAllowances(List<Allowance> allowances, String companyCode);

    void updateStatus(List<String> codes, String status, String companyCode);
}
