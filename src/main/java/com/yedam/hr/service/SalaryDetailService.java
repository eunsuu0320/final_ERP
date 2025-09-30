package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.SalaryDetail;

public interface SalaryDetailService {

	// 급여 디테일 회사코드별 조회
	List<SalaryDetail> getSalaryDetails(String companyCode);
}
