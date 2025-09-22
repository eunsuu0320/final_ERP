package com.yedam.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.yedam.domain.Product;
import com.yedam.service.ProductService;

@Controller
public class Sales1Controller {

	
    private final ProductService productService;

    @Autowired
    public Sales1Controller(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("itemList")
    public String itemList(Model model) {
        // 1. 전체 상품 조회
        List<Product> products = productService.getAllProduct();

        // 2. 테이블 데이터 변환
        Map<String, Object> tableData = productService.getTableDataFromProducts(products);

        // 3. 모델에 담기
        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/itemList"; // => templates/sales1/itemList.html
    }
}
