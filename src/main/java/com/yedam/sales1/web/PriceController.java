package com.yedam.sales1.web;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;
import com.yedam.sales1.domain.PriceDetail;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.service.PartnerService;
import com.yedam.sales1.service.PriceService;
import com.yedam.sales1.service.ProductService;

@Controller
public class PriceController {

	private final PriceService priceService;
	private final PartnerService partnerService;
	private final ProductService productService;

	@Autowired
	public PriceController(PriceService priceService, PartnerService partnerService, ProductService productService) {
		this.priceService = priceService;
		this.partnerService = partnerService;
		this.productService = productService;
	}

	@GetMapping("priceList")
	public String priceList(Model model) {
		List<Price> prices = priceService.getAllPrice();

		Map<String, Object> tableData = priceService.getTableDataFromPrice(prices);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/priceList";
	}

	// 단가그룹의 거래처 리스트 받아오기
	@GetMapping("api/price/getPartner")
	@ResponseBody
	public List<String> pricePartnerList(@RequestParam("priceUniqueCode") Integer priceUniqueCode) {
		// priceService.getAllPartner(priceCode)는 List<String>을 반환한다고 가정
		return priceService.getAllPartner(priceUniqueCode);
	}

	// 거래처설정 Modal
	@GetMapping("api/price/getAllPartner")
	@ResponseBody
	public Map<String, Object> pricePartnerModalList() {
		List<Partner> partners = partnerService.getAllPartner();

		// 서비스에서 Map<String, Object> 형태로 데이터를 가공하여 반환
		return priceService.getTableDataFromPartnerModal(partners);
	}

	// 단가그룹의 품목 리스트 받아오기
	@GetMapping("api/price/getProduct")
	@ResponseBody
	public List<String> priceProductList(@RequestParam("priceUniqueCode") Integer priceUniqueCode) {
		// priceService.getAllPartner(priceCode)는 List<String>을 반환한다고 가정
		return priceService.getAllProduct(priceUniqueCode);
	}

	// 품목설정 Modal
	@GetMapping("api/price/getAllProduct")
	@ResponseBody
	public Map<String, Object> priceProductModalList() {
		List<Product> products = productService.getAllProduct();

		// 서비스에서 Map<String, Object> 형태로 데이터를 가공하여 반환
		return priceService.getTableDataFromProductModal(products);
	}

	@GetMapping("api/pricetabPartner")
	@ResponseBody // JSON 반환을 명시 (클래스에 @RestController가 있다면 생략 가능)
	public Map<String, Object> pricePartnerList() { // Model 제거
		List<Price> prices = priceService.getAllPricePartner();

		// 서비스에서 Map<String, Object> 형태로 데이터를 가공하여 반환
		return priceService.getTableDataFromPartners(prices);
	}

	// -----------------------------------------------------------
	// 3. 품목 탭 데이터 로드 (JSON 데이터 반환 - 수정됨)
	// -----------------------------------------------------------
	@GetMapping("api/pricetabProduct")
	@ResponseBody // JSON 반환을 명시
	public Map<String, Object> priceProductList() { // Model 제거
		List<Price> prices = priceService.getAllPriceProduct();

		// 서비스에서 Map<String, Object> 형태로 데이터를 가공하여 반환
		return priceService.getTableDataFromProducts(prices);
	}

	// 품목 상세정보 조회
	@GetMapping("api/price/getDetail")
	public ResponseEntity<Price> getPrice(@RequestParam String keyword) {
		System.out.println("controller 확인");
		System.out.println("조회 PriceGroupCode: " + keyword);

		Price price = priceService.getPriceByPriceGroupCode(keyword);
		System.out.println("============================================================");
		System.out.println("price 상세정보 조회 결과");


		if (price != null) {
			return ResponseEntity.ok(price);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 품목 등록
	@PostMapping("api/registPrice")
	public ResponseEntity<Price> registPrice(@ModelAttribute Price price) {
		Price saved = priceService.savePrice(price);
		return ResponseEntity.ok(saved);
	}

	@GetMapping("api/priceList")
	@ResponseBody
	public Map<String, Object> priceAllList() { // 메서드 이름 변경 가능
		List<Price> prices = priceService.getAllPrice();
		return priceService.getTableDataFromPrice(prices);
	}

	// =========================================================================
	// 거래처 설정 저장 (savePartners)
	// =========================================================================
	@PostMapping("/api/price/savePartners")
	public ResponseEntity<?> registPartner(@RequestBody Map<String, Object> payload) {
		System.out.println("==============================================================");
		System.out.println("payload: " + payload);

		Integer priceCode = null;
		try {
			priceCode = Integer.parseInt(payload.get("priceUniqueCode").toString()); // ✅ 문자열 안전 변환
		} catch (Exception e) {
			System.err.println("❌ priceUniqueCode 변환 실패: " + payload.get("priceUniqueCode"));
			e.printStackTrace(); // 🔥 상세 원인 출력
			return ResponseEntity.badRequest().body("잘못된 priceUniqueCode 값입니다: " + payload.get("priceUniqueCode"));
		}

		@SuppressWarnings("unchecked")
		List<String> partnerCodes = (List<String>) payload.get("partnerCodes");

		try {
			PriceDetail saved = priceService.savePriceDetailPartner(priceCode, partnerCodes);
			return ResponseEntity.ok(saved);
		} catch (Exception e) {
			e.printStackTrace(); // 🔥 콘솔에 상세 원인 출력
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 처리 중 오류 발생: " + e.getMessage());
		}
	}

	// =========================================================================
	// 품목 설정 저장 (saveProducts)
	// =========================================================================
	@PostMapping("/api/price/saveProducts")
	// @RequestBody Map을 사용하여 JSON 본문을 전체 맵으로 받습니다.
	public ResponseEntity<?> registProduct(@RequestBody Map<String, Object> payload) {
		Integer priceCode = null;
		try {
			priceCode = Integer.parseInt(payload.get("priceUniqueCode").toString()); // ✅ 문자열 안전 변환
		} catch (Exception e) {
			System.err.println("❌ priceUniqueCode 변환 실패: " + payload.get("priceUniqueCode"));
			e.printStackTrace(); // 🔥 상세 원인 출력
			return ResponseEntity.badRequest().body("잘못된 priceUniqueCode 값입니다: " + payload.get("priceUniqueCode"));
		}

		@SuppressWarnings("unchecked")
		List<String> productCodes = (List<String>) payload.get("productCodes");

		try {
			PriceDetail saved = priceService.savePriceDetailProduct(priceCode, productCodes);
			return ResponseEntity.ok(saved);
		} catch (Exception e) {
			e.printStackTrace(); // 🔥 콘솔에 상세 원인 출력
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 처리 중 오류 발생: " + e.getMessage());
		}

	}

	@GetMapping("api/price/search")
	// 반환 타입을 Map 리스트로 변경해야 합니다.
	public ResponseEntity<List<Map<String, Object>>> getPriceSearch(@ModelAttribute Price searchVo) {

		System.out.println("조회 조건 Price VO: " + searchVo);

		// 1. 기존처럼 필터링된 Product VO 리스트를 가져옵니다. (영문 키)
		List<Price> prices = priceService.getFilterPrice(searchVo);

		// 2. ★★★ 핵심 수정: 영문 키 리스트를 한글 키 Map으로 변환합니다. ★★★
		Map<String, Object> tableData = priceService.getTableDataFromPrice(prices);

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
