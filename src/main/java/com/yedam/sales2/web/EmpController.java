package com.yedam.sales2.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.domain.EspPlan;
import com.yedam.sales2.service.empService;
import com.yedam.sales2.service.salesService;

@Controller
public class EmpController {
	
	@Autowired
	 private empService empService;

	// 사원별 영업계획 HTML
	@GetMapping("empList")
	public String empList() {
		return "sales2/empList";
	}
	
	// JOSN 데이터를 반환하는 API
	@GetMapping("empJson")
	@ResponseBody
	public List<EspPlan> empJson() {
		return empService.findAll();
	}
}
