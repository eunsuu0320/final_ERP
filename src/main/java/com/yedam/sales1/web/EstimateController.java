package com.yedam.sales1.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.service.EstimateService;

@Controller
public class EstimateController {

	
    private final EstimateService estimateService;

    @Autowired
    public EstimateController(EstimateService estimateService) {
        this.estimateService = estimateService;
    }

    @GetMapping("estimateList")
    public String estimateList(Model model) {
        List<Estimate> estimate = estimateService.getAllEstimate();
        
        Map<String, Object> tableData = estimateService.getTableDataFromEstimate(estimate);

        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/estimateList";
    }
    
    
    // 품목 등록
    @PostMapping("api/registEstimate")
    public ResponseEntity<Estimate> registOrders(@ModelAttribute Estimate estimate) {
    	Estimate saved = estimateService.saveEstimate(estimate);
        return ResponseEntity.ok(saved);
    }
    
}
