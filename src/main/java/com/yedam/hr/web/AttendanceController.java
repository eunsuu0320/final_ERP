package com.yedam.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.Attendance;
import com.yedam.hr.service.AttendanceService;

import ch.qos.logback.core.model.Model;

@Controller
public class AttendanceController {

	@Autowired AttendanceService attendanceService;

	// 근태 관리 페이지
	@GetMapping("/attendancePage")
	public String getAttendancePage(Model model) {
		return "hr/attendance";
	}

	// 회사코드별 근태 목록 조회
	@GetMapping("/attendance")
	@ResponseBody
	public List<Attendance> getAttendances (@RequestParam String companyCode) {
		return attendanceService.getAttendances(companyCode);
	}

}
