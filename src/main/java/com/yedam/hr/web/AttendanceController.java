package com.yedam.hr.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.Attendance;
import com.yedam.hr.service.AttendanceService;

import ch.qos.logback.core.model.Model;

@Controller
public class AttendanceController {

	@Autowired
	AttendanceService attendanceService;

	// 근태 관리 페이지
	@GetMapping("/attendancePage")
	public String getAttendancePage(Model model) {
		return "hr/attendance";
	}

	// 회사코드별 근태 항목 목록 조회
	@GetMapping("/attendance")
	@ResponseBody
	public List<Attendance> getAttendances(@RequestParam String companyCode) {
		return attendanceService.getAttendances(companyCode);
	}

	// 근태 항목 여러 건 저장
	@PostMapping("/attendance/saveAll")
	@ResponseBody
	public String saveAttendanceAll(@RequestBody List<Attendance> attendances, @RequestParam String companyCode) {
		try {
			attendanceService.saveAllAttendances(attendances, companyCode);
			return "success";
		} catch (Exception e) {
			e.printStackTrace();
			return "fail: " + e.getMessage();
		}
	}

	// 근태 항목 사용 중단 및 재사용
	@PostMapping("/attendance/updateStatus")
	@ResponseBody
	public String postMethodName(@RequestBody Map<String, Object> payload,
			@RequestParam String companyCode) {
		List<String> codes = (List<String>) payload.get("codes");
		String status = (String) payload.get("status");

		try {
			attendanceService.updateStatus(codes, status, companyCode);
			return "success";
		} catch (Exception e) {
			e.printStackTrace();
			return "fail: " + e.getMessage();
		}
	}

}
