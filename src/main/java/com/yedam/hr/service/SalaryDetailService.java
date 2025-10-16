package com.yedam.hr.service;

import java.util.List;
import java.util.Map;

import com.yedam.hr.domain.SalaryDetail;

public interface SalaryDetailService {

	// 급여 디테일 회사코드별 조회
	List<SalaryDetail> getSalaryDetails(String companyCode);

	// 회사코드별, 급여대장별 조회
	Map<String, Object> getSalaryDetailBundle(String companyCode, String salaryId);
}
