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
import org.springframework.web.bind.annotation.ResponseBody; // JSON 응답을 명시적으로 위해 추가할 수 있습니다.
import org.springframework.web.multipart.MultipartFile;

import com.yedam.common.ScreenPerm;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.service.ProductService;

@Controller
public class ProductController {

	private final ProductService productService;

	@Autowired
	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	/**
	 * 초기 페이지 로딩: JSP 뷰를 반환하며, Model에 초기 데이터(컬럼/로우)를 담아 전달합니다.
	 */
	@ScreenPerm(screen = "SAL_ITEM", action = ScreenPerm.Action.READ)
	@GetMapping("productList")
	public String productList(Model model) {
		List<Product> products = productService.getAllProduct();

		// JSP에서 Tabulator를 초기화하는 데 사용될 초기 데이터
		Map<String, Object> tableData = productService.getTableDataFromProducts(products);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/productList";
	}

	// (품목 등록/수정 API - 기존 코드 유지)
	@ScreenPerm(screen = "SAL_ITEM", action = ScreenPerm.Action.CREATE)
	@PostMapping("api/registProduct")
	public ResponseEntity<Product> registProduct(@ModelAttribute Product product,
			@RequestParam(value = "productImage", required = false) MultipartFile file) {
		// Service 메서드 호출 시 파일도 함께 전달
		Product saved = productService.saveProduct(product, file);

		return ResponseEntity.ok(saved);
	}

	@ScreenPerm(screen = "SAL_ITEM", action = ScreenPerm.Action.UPDATE)
	@PostMapping("api/modifyProduct")
	public ResponseEntity<Product> modifyProduct(@ModelAttribute Product product,
			@RequestParam(value = "productImage", required = false) MultipartFile file) {
		Product saved = productService.saveProduct(product, file);

		return ResponseEntity.ok(saved);
	}

	// (품목 상세정보 조회 API - 기존 코드 유지)
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


	/**
	 * 품목 필터링 및 전체 조회 API (초기화 및 검색 기능 모두 이 API를 사용합니다.)
	 */
	@GetMapping("api/product/search")
	// 반환 타입을 Map 리스트로 변경해야 합니다.
	public ResponseEntity<List<Map<String, Object>>> getProductSearch(@ModelAttribute Product searchVo) {

		System.out.println("조회 조건 Product VO: " + searchVo);

		// 1. 기존처럼 필터링된 Product VO 리스트를 가져옵니다. (영문 키)
		List<Product> products = productService.getFilterProduct(searchVo);

		// 2. ★★★ 핵심 수정: 영문 키 리스트를 한글 키 Map으로 변환합니다. ★★★
		Map<String, Object> tableData = productService.getTableDataFromProducts(products);

		// 3. Map에서 Tabulator가 필요로 하는 'rows' (한글 키 리스트)만 추출합니다.
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> rows = (List<Map<String, Object>>) tableData.get("rows");

		if (rows != null && !rows.isEmpty()) {
			// 4. 한글 키 Map 리스트를 JSON 형태로 반환합니다.
			return ResponseEntity.ok(rows);
		} else {
			// 검색 결과가 없는 경우, 빈 리스트를 JSON 형태로 반환
			return ResponseEntity.ok(Collections.emptyList());
		}
	}
}