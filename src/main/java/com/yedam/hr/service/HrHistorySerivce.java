package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.dto.HrHistoryDTO;

public interface HrHistorySerivce {
	// 이력 조회
	List<HrHistoryDTO> findByCompanyCode(String companyCode);

}
