package com.yedam.hr.service;

import java.util.List;

import com.yedam.hr.domain.Attendance;

public interface AttendanceService {

	// 회사코드별 근태 목록 조회
	List<Attendance> getAttendances(String companyCode);
}
