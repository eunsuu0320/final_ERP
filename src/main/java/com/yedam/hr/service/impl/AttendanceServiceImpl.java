package com.yedam.hr.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.hr.domain.Attendance;
import com.yedam.hr.repository.AttendanceRepository;
import com.yedam.hr.service.AttendanceService;

@Service
public class AttendanceServiceImpl implements AttendanceService {

	@Autowired AttendanceRepository attendanceRepository;

	@Override
	public List<Attendance> getAttendances(String companyCode) {
		return attendanceRepository.findByCompanyCode(companyCode);
	}
}
