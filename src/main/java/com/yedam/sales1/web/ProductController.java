package com.yedam.sales1.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.yedam.hr.domain.Employee;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.service.ProductService;

@Controller
public class ProductController {

	
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
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

        return "sales1/productList"; // => templates/sales1/itemList.html
    }
    
}
