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

	@Override
	public List<Attendance> saveAllAttendances(List<Attendance> attendances, String companyCode) {
		attendances.forEach(a -> a.setCompanyCode(companyCode));
		return attendanceRepository.saveAll(attendances);
	}

	@Override
	public void updateStatus(List<String> codes, String status, String companyCode) {
		for (String code : codes) {
			Attendance attendance = attendanceRepository.findByAttIdAndCompanyCode(code, companyCode)
					.orElseThrow(() -> new RuntimeException("해당 근태 없음: " + code));

			attendance.setAttIs(status);
			attendanceRepository.save(attendance);
		}

	}
}
