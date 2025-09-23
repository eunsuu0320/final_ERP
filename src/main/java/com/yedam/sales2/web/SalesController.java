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

	@GetMapping("salesList")
    public String salesList() {
        return "sales2/salesList";
    }
	
	// JSON 데이터를 반환하는 API
    @GetMapping("salesJson")
    @ResponseBody // 이 어노테이션이 있어야 Spring이 JSON 데이터로 인식합니다.
    public List<Sales> salesJson() {
        return salesService.findAll();
    }
    
    // 기존의 salesJson 메서드 대신, 통계 데이터를 반환하는 새로운 메서드를 추가
    @GetMapping("/api/sales/stats")
    @ResponseBody
    public List<Map<String, Object>> getSalesStats() {
        // 서비스 레이어에서 연도별 통계 데이터를 가져오는 메서드를 호출
        return salesService.findSalesStatsByYear(); 
    }
    
	
	
}
