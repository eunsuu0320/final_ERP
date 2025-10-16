// src/main/java/com/yedam/sales3/web/SalesDashController.java
package com.yedam.sales3.web;

import java.time.Year;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales3.domain.dto.MetricsSummary;
import com.yedam.sales3.domain.dto.QuarterlyProfitRatePoint;
import com.yedam.sales3.domain.dto.TopEmployeeSales;
import com.yedam.sales3.domain.dto.TopPartnerSales;
import com.yedam.sales3.domain.dto.YearlyProfitPoint;
import com.yedam.sales3.service.SalesDashService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping
public class SalesDashController {

    private final SalesDashService service;

    // 화면
    @GetMapping("/sales3/salesdash")
    public String view(Model model, @RequestParam(required=false) String companyCode){
        // 회사코드는 인터셉터/세션에서 내려주는 값이 있으면 사용하고, 없으면 파라미터/기본값
        String cc = (companyCode==null || companyCode.isBlank()) ? "C001" : companyCode;
        model.addAttribute("companyCode", cc);
        model.addAttribute("currentYear", Year.now().getValue());
        return "sales3/salesdash";
    }

    // === REST APIs ===
    @GetMapping("/api/sales3/dashboard/metrics")
    @ResponseBody
    public MetricsSummary metrics(@RequestParam String companyCode){
        return service.getMetrics(companyCode);
    }

    @GetMapping("/api/sales3/dashboard/yearly-profit")
    @ResponseBody
    public List<YearlyProfitPoint> yearly(@RequestParam String companyCode){
        return service.getYearlyProfitLast5(companyCode);
    }

    @GetMapping("/api/sales3/dashboard/quarterly-profit-rate")
    @ResponseBody
    public List<QuarterlyProfitRatePoint> quarterly(@RequestParam String companyCode,
                                                    @RequestParam(required=false) Integer year){
        int y = (year==null)? Year.now().getValue() : year;
        return service.getQuarterlyProfitRate(companyCode, y);
    }

    @GetMapping("/api/sales3/dashboard/top-partners")
    @ResponseBody
    public List<TopPartnerSales> topPartners(@RequestParam String companyCode){
        return service.getTop5Partners(companyCode);
    }

    @GetMapping("/api/sales3/dashboard/top-employees")
    @ResponseBody
    public List<TopEmployeeSales> topEmployees(@RequestParam String companyCode){
        return service.getTop5Employees(companyCode);
    }
}
