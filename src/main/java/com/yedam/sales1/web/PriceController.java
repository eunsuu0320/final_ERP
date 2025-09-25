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

import com.yedam.sales1.domain.Price;
import com.yedam.sales1.service.PriceService;

@Controller
public class PriceController {

	
    private final PriceService priceService;

    @Autowired
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("priceList")
    public String priceList(Model model) {
        List<Price> prices = priceService.getAllPrice();
        
        Map<String, Object> tableData = priceService.getTableDataFromPrice(prices);

        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/priceList";
    }
    
    
    // 품목 등록
    @PostMapping("api/registPrice")
    public ResponseEntity<Price> registPrice(@ModelAttribute Price price) {
    	Price saved = priceService.savePrice(price);
        return ResponseEntity.ok(saved);
    }
    
}
