package com.yedam.sales2.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.domain.EspPlan;
import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.service.EmpService;

/*
 * 사원별 매출계획 관리
 */

@Controller
public class EmpPlanController {
	
	@Autowired
	 private EmpService empService;

	// 사원별 영업계획 HTML
	@GetMapping("empList")
	public String empList() {
		return "sales2/empList";
	}
	
	// 등록 (AJAX로 호출)
    @PostMapping("/api/sales/insertEmpPlan")
    @ResponseBody
    public EspPlan insertPlan(@RequestBody EspPlan espPlan) {
        return empService.insertSalePlan(espPlan);
    }
}	
