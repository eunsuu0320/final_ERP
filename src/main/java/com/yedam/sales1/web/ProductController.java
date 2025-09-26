package com.yedam.sales1.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.yedam.sales1.domain.Product;
import com.yedam.sales1.service.ProductService;

@Controller
public class ProductController {

	private final ProductService productService;

	@Autowired
	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("productList")
	public String productList(Model model) {
		List<Product> products = productService.getAllProduct();

		Map<String, Object> tableData = productService.getTableDataFromProducts(products);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/productList";
	}

	// 품목 등록
	@PostMapping("api/registProduct")
	public ResponseEntity<Product> registProduct(@ModelAttribute Product product) {
		Product saved = productService.saveProduct(product);
		return ResponseEntity.ok(saved);
	}

	
	// 품목 상세정보 조회
	@GetMapping("api/getProduct")
	public ResponseEntity<Product> getProduct(@RequestParam String productCode) {
		System.out.println("controller 확인");
		System.out.println("조회 productCode: " + productCode);

		Product product = productService.getProductByProductCode(productCode);
		if (product != null) {
			return ResponseEntity.ok(product);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	// 품목 이력 조회
	@GetMapping("api/product/history")
	public ResponseEntity<Product> getProductHistory(@RequestParam String productCode) {
		System.out.println("controller 확인");
		System.out.println("조회 productCode: " + productCode);

		Product product = productService.getProductByProductCode(productCode);
		if (product != null) {
			return ResponseEntity.ok(product);
		} else {
			return ResponseEntity.notFound().build();
		}
	}
	
	
	

}
