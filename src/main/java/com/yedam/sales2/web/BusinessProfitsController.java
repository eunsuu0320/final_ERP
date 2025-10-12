package com.yedam.sales2.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.service.BusinessProfitsService;
import com.yedam.sales2.service.SalesService;

/*
 * 제품별, 사원별 영업이익조회
 */
@Controller
public class BusinessProfitsController {

	 @Autowired
	    private SalesService salesService;
	 
	 @Autowired
	    private BusinessProfitsService businessProfitsService;
	
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
	
	// 품목별 영업이익 조회 API
    @GetMapping("/api/sales/profit-list")
    @ResponseBody
    public List<Map<String, Object>> getSalesProfitList(
            @RequestParam(required = false) String year,
            @RequestParam(required = false) String quarter,
            @RequestParam(required = false) String keyword
    ) {
    	return businessProfitsService.getSalesProfitList(year, quarter, keyword);
    }
    
    
	
}
