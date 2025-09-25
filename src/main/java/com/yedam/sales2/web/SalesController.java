package com.yedam.sales2.web;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
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
        return salesService.findSalesPlanData(); 
    }
    
    @Autowired
    public SalesController(salesService salesService) {
        this.salesService = salesService;
    }
    
    @Autowired
    private SalesPlanRepository salesPlanRepository;

    @Autowired
    private DSalesPlanRepository dSalesPlanRepository;
    
 // 메인 + 세부 등록
    @PostMapping("/api/sales/insert")
    public String insertSalesPlan(@RequestBody List<DSalesPlan> detailList) {
        try {
            if (detailList == null || detailList.isEmpty()) {
                return "error: 세부 데이터 없음";
            }

            // 1️⃣ 메인 테이블 저장
            SalesPlan master = new SalesPlan();
            master.setPlanYear(new Date()); // JS에서 planYear를 보내면 그걸로 세팅 가능
            master.setRegDate(new Date());
            master.setEmpCode("EMP001");      // JS에서 받으면 변경
            master.setCompanyCode("COMP001");  // JS에서 받으면 변경
            salesPlanRepository.save(master);

            // 2️⃣ 세부 테이블 저장 (FK 연결)
            for (DSalesPlan detail : detailList) {
                // FK 설정 (salesPlanCode)
                detail.setSalesPlanDetailCode(0); // 시퀀스 자동 생성
                // detail에 FK 컬럼이 필요하면 아래처럼 매핑
                // detail.setSalesPlanCode(master.getSalesPlanCode());
            }

            dSalesPlanRepository.saveAll(detailList);

            return "sales2/salesList";
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
	
}
