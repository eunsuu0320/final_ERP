package com.yedam.sales2.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.sales2.service.BusinessProfitsService;
import com.yedam.sales2.service.EmployeeProfitsService;
import com.yedam.sales2.service.SalesService;

import lombok.RequiredArgsConstructor;

/*
 * 사원별 영업이익조회
 */
@Controller
@RequestMapping("/api/employeeProfits")
public class EmployeeProfitsController {

	 @Autowired
	    private EmployeeProfitsService service;

	    @ResponseBody
	    @GetMapping("/list")
	    public List<Map<String, Object>> getEmployeeProfitsList(
	        @RequestParam String companyCode,
	        @RequestParam(required = false) Integer year,
	        @RequestParam(required = false) Integer quarter,
	        @RequestParam(required = false) String keyword
	    ) {
	        return service.getEmployeeSummary(companyCode, year, quarter, keyword);
	    }
	}