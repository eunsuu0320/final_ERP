package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.SalaryMaster;

public interface SalaryMasterService {

	// 급여 마스터 회사코드별 조회
	List<SalaryMaster> getSalaryMasters(String companyCode);


}
