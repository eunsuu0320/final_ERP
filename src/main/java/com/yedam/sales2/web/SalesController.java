package com.yedam.sales2.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.service.salesService;

@Controller
public class SalesController {
	
	@Autowired
	 private salesService salesService;

	// 영업계획목록 html
	@GetMapping("salesList")
    public String salesList() {
        return "sales2/salesList";
    }
	
	// JSON 데이터를 반환하는 API
    @GetMapping("salesJson")
    @ResponseBody 
    public List<Sales> salesJson() {
        return salesService.findAll();
    }
    
    @GetMapping("/api/sales/stats")
    @ResponseBody
    public List<Map<String, Object>> getSalesStats() {
        // 서비스 레이어에서 연도별 통계 데이터를 가져오는 메서드를 호출
        return salesService.findSalesStatsByYear(); 
    }
    
    // 영업계획등록 html
    @GetMapping("insertSales")
    public String insertSales() {
    	return "sales2/insertSalesModal";
    }
    
    // Tabulator의 ajaxURL과 일치하도록 경로 수정
    @GetMapping("/api/sales/last-year-qty")
    @ResponseBody
    public List<Map<String, Object>> insertsSalesStats() {
        // 서비스 레이어에서 연도별 통계 데이터를 가져오는 메서드를 호출
        return salesService.findSalesPlanData(); 
    }
	
}
