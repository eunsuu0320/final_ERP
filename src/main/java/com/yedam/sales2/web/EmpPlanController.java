package com.yedam.sales2.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales2.domain.EsdpPlan;
import com.yedam.sales2.domain.EspPlan;
import com.yedam.sales2.domain.PlanRequestDTO;
import com.yedam.sales2.service.EmpService;
import com.yedam.sales2.service.SalesService;

/*
 * 사원별 매출계획 관리
 */

@Controller
public class EmpPlanController {
	
	@Autowired
	 private EmpService empService;
	
	@Autowired
	 private SalesService salesService;

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
    
    
 // 사원별 세부계획 등록 (분기별 여러 건)
    @PostMapping("/api/sales/insertPlanWithDetails")
    @ResponseBody
    public String insertPlanWithDetails(@RequestBody PlanRequestDTO request) {
        // 1. 상위 계획 저장
        //EspPlan esp = new EspPlan();
//        esp.setEmpCode(request.getEmpCode());
//        esp.setCompanyCode(request.getCompanyCode());
//        EspPlan saved = empService.insertSalePlan(esp);

        // 2. 하위 세부 계획들에 부모 코드 세팅 후 저장
//        for (EsdpPlan detail : request.getDetailPlans()) {
//            detail.setEspCode(saved.getEspCode()); // FK 세팅
//        }
        empService.insertDetailPlans(request.getDetailPlans());

        return "OK";
    }

 	// 사원영업계획조회
 	@GetMapping("/api/slaes/empDeatilPlan")
 	@ResponseBody
 	public List<EsdpPlan> getDetailList(String espCode) {
 		return empService.findByEspCode(espCode);
 	}
    
}	
