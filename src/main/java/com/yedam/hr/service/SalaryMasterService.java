package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.SalaryMaster;

public interface SalaryMasterService {

	// 급여 마스터 회사코드별 조회
	List<SalaryMaster> getSalaryMasters(String companyCode);

	// 급여대장 마스터 + 급여대장 디테일(금액 전체 0)으로 등록
	SalaryMaster insertSalaryMaster(SalaryMaster salaryMaster);

	int confirmSelected(String companyCode, List<String> salaryIds);

	String findByCompanyCodeAndSalaryId(String companyCode, String salaryId);
}
