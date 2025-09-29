package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.HrHistory;

public interface HrHistorySerivce {
	// 이력 조회
	List<HrHistory> findByCompanyCode(String companyCode);

}
