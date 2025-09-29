package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.Dedcut;

public interface DedcutService {

	// 공제 조회
	List<Dedcut> findByCompanyCode(String companyCode);

	// 여러 건 및 단 건 저장
	List<Dedcut> saveAllDedcuts(List<Dedcut> dedcuts, String companyCode);

	void updateStatus(List<Dedcut> codes, String status, String companyCode);
}
