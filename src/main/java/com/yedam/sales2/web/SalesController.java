package com.yedam.sales2.web;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.domain.DSalesPlan;
import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.domain.SalesPlan;
import com.yedam.sales2.repository.DSalesPlanRepository;
import com.yedam.sales2.repository.SalesPlanRepository;
import com.yedam.sales2.repository.SalesRepository;
import com.yedam.sales2.service.salesService;

@Controller
public class SalesController {
	
	@Autowired
	 private salesService salesService;
		
	@Autowired
	private SalesRepository salesRepository; 

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
        return salesService.findLastYearSalesData(); 
    }
    
    @Autowired
    public SalesController(salesService salesService) {
        this.salesService = salesService;
    }
    
    @Autowired
    private SalesPlanRepository salesPlanRepository;

    @Autowired
    private DSalesPlanRepository dSalesPlanRepository;
    
    @PostMapping("/api/sales/insert")
    public String insertSalesPlan(@RequestBody List<DSalesPlan> detailList) {
        try {
            if (detailList == null || detailList.isEmpty()) {
                return "error: 세부 데이터 없음";
            }

            // 1. 메인 테이블 저장: JPA가 salesPlanCode를 자동으로 채워줍니다.
            SalesPlan master = new SalesPlan();
            master.setPlanYear(new Date());
            master.setRegDate(new Date());
            master.setEmpCode("EMP001");
            master.setCompanyCode("COMP001");
            salesPlanRepository.save(master);

            // 2. 세부 테이블에 외래 키(FK) 연결
            for (DSalesPlan detail : detailList) {
                // 부모 객체 자체를 자식 객체의 salesPlan 필드에 연결합니다.
                detail.setSalesPlan(master);
            }

            // 3. 모든 세부 테이블 데이터 한 번에 저장
            dSalesPlanRepository.saveAll(detailList);

            return "sales2/salesList";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
    
    @GetMapping("/api/sales/check-this-year")
    @ResponseBody
    public Map<String, Boolean> checkThisYear() {
        int currentYear = LocalDate.now().getYear();
        boolean exists = salesPlanRepository.existsByPlanYear(currentYear); // JPA 메서드 사용
        return Map.of("exists", exists);
    }

    // 연도별 영업계획 조회
    @GetMapping("/api/sales/plan/{year}")
    @ResponseBody
    public List<SalesPlan> getPlanByYear(@PathVariable int year) {
        return salesService.getPlanByYear(year);
    }
}
