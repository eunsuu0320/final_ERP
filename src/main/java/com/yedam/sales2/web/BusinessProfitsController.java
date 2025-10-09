package com.yedam.sales2.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * 제품별, 사원별 영업이익조회
 */
@Controller
public class BusinessProfitsController {

	
	// 영업이익조회 HTML
	@GetMapping("businessProfits")
	public String businessProfits() {
		return "sales2/businessProfits";
	}
	
	// 사원별이익조회 HTML
	@GetMapping("employeeProfits")
		public String employeeProfits() {
			return "sales2/employeeProfits";
		}
	
}
