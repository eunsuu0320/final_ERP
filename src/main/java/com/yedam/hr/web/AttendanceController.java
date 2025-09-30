package com.yedam.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import ch.qos.logback.core.model.Model;

@Controller
public class AttendanceController {

	@GetMapping("/attendancePage")
	public String getAttendancePage(Model model) {
		return "hr/attendance";
	}

}
