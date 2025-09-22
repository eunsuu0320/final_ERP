package com.yedam.hr.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HrController {

	@GetMapping("/employee")
	public String employee(Model model) {
		return "hr/employee";
	}
}
