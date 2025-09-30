package com.yedam.sales2.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.domain.DSalesPlan;
import com.yedam.sales2.domain.EmpPlan;
import com.yedam.sales2.domain.SalesPlan;
import com.yedam.sales2.service.SalesService;

/*
 * 영업계획 관리
 */
@Controller
public class SalesPlanController {
	
	@Autowired
	 private SalesService salesService;
		
	// 영업계획목록 html
	@GetMapping("salesList")
    public String salesList() {
        return "sales2/salesList";
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
    
    // 📌 신규 등록: Repository를 직접 사용하지 않고 Service 레이어로 위임
    @PostMapping("/api/sales/insert")
    @ResponseBody // 결과를 JSON 문자열로 반환
    public String insertSalesPlan(@RequestBody List<DSalesPlan> detailList) {
        try {
            // Service 계층으로 등록 로직 위임
            String result = salesService.insertSalesPlan(detailList);
            return result; // "success" 반환
        } catch (Exception e) {
            e.printStackTrace();
            // 클라이언트에게 오류 메시지를 명확히 전달
            return "error: " + e.getMessage();
        }
    }
    
    // 📌 올해 영업계획 존재 여부 확인: Service로 위임
    @GetMapping("/api/sales/check-this-year")
    @ResponseBody
    public Map<String, Boolean> checkThisYear() {
        int currentYear = LocalDate.now().getYear();
        // Service 메서드 호출
        boolean exists = salesService.checkSalesPlanExists(currentYear); 
        return Map.of("exists", exists);
    }

    // 연도별 영업계획 조회
    @GetMapping("/api/sales/plan/{year}")
    @ResponseBody
    public List<SalesPlan> getPlanByYear(@PathVariable int year) {
        return salesService.getPlanByYear(year);
    }
    
    // 수정 주소 매핑
    @GetMapping("/api/sales/plan/{year}/details")
    @ResponseBody
    public List<DSalesPlan> getSalesPlanDetails(@PathVariable int year) {
        // Service에서 @Transient 필드에 salesPlanCode를 채워서 반환해줍니다.
        return salesService.getSalesPlanDetail(year);
    }
    
    // 수정 (PUT)
    @PutMapping("/api/sales/update")
    @ResponseBody
    public String updateSalesPlan(@RequestBody List<DSalesPlan> detailList) {
        try {
            if (detailList == null || detailList.isEmpty()) {
                return "error: 세부 데이터 없음";
            }

            // 📌 DSalesPlan 객체의 Transient 필드에서 planCode를 가져옵니다.
            // DSalesPlan 엔티티 수정으로 인해 NullPointerException 위험이 줄어들었습니다.
            Integer planCode = detailList.get(0).getSalesPlanCode(); 
            
            if (planCode == null) {
                 return "error: Sales Plan Code is missing in the request data.";
            }

            String result = salesService.updateSalesPlanDetails(planCode, detailList);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return "error: " + e.getMessage();
        }
    }
    
 // 사원별 전년 영업매출
 	@GetMapping("/api/sales/empPlanList")
 	@ResponseBody
 	public List<Map<String, Object>> getEmpList() {
 		return salesService.getEmpPlanList();
 	}
}
