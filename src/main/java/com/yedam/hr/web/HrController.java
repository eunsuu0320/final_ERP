package com.yedam.hr.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.hr.domain.Employee;
import com.yedam.hr.service.HrService;

@Controller
public class HrController {

	@Autowired HrService hrService;

	// 사원 등록
	@GetMapping("/empPage")
	public String getEmployee (Model model) {
		return "hr/employee";
	}

	@ResponseBody
	@GetMapping("/selectAllEmp")
	public List<Employee> employee (Model model) {
		return hrService.getAllEmployees();
	}

	// 수당 및 공제 관리
	@GetMapping("/allowDeduct")
	public String getAllowDeduct (Model model) {
		return "hr/allowDeduct";
	}
}
