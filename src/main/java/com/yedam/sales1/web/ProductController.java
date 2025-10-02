package com.yedam.sales1.web;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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

	@PostMapping("api/registProduct")
	public ResponseEntity<Product> registProduct(@ModelAttribute Product product,
			@RequestParam(value = "productImage", required = false) MultipartFile file) {
		// Service 메서드 호출 시 파일도 함께 전달
		Product saved = productService.saveProduct(product, file);

		return ResponseEntity.ok(saved);
	}

	@PostMapping("api/modifyProduct")
	public ResponseEntity<Product> modifyProduct(@ModelAttribute Product product,
			@RequestParam(value = "productImage", required = false) MultipartFile file) {
		Product saved = productService.saveProduct(product, file);

		return ResponseEntity.ok(saved);
	}

	// 품목 상세정보 조회
	@GetMapping("api/product/getDetail")
	public ResponseEntity<Product> getProduct(@RequestParam String keyword) {
		System.out.println("controller 확인");
		System.out.println("조회 productCode: " + keyword);

		Product product = productService.getProductByProductCode(keyword);
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

	// 품목 필터링
	@GetMapping("api/product/search")
	public ResponseEntity<List<Product>> getProductSearch(@ModelAttribute Product searchVo) {

		System.out.println("조회 조건 Product VO: " + searchVo);

		// searchVo.getProductName() 등으로 바로 접근하여 Service 로직에 사용
		List<Product> products = productService.getFilterProduct(searchVo);

		if (products != null && !products.isEmpty()) {
			return ResponseEntity.ok(products);
		} else {
			return ResponseEntity.ok(Collections.emptyList());
		}
	}
}
