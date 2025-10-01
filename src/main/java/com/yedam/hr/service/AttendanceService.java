package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.Attendance;

public interface AttendanceService {

	// 회사코드별 근태 목록 조회
	List<Attendance> getAttendances(String companyCode);

	// 여러 건 및 단 건 저장
	List<Attendance> saveAllAttendances(List<Attendance> attendances, String companyCode);

	// 사용중단 및 재사용
	void updateStatus(List<String> codes, String status, String companyCode);
}
