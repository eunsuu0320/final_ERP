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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

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
	
	@GetMapping("api/pricetabPartner")
	@ResponseBody // JSON 반환을 명시 (클래스에 @RestController가 있다면 생략 가능)
	public Map<String, Object> pricePartnerList() { // Model 제거
	    List<Price> prices = priceService.getAllPrice();

	    // 서비스에서 Map<String, Object> 형태로 데이터를 가공하여 반환
	    return priceService.getTableDataFromPartners(prices); 
	}

	// -----------------------------------------------------------
	// 3. 품목 탭 데이터 로드 (JSON 데이터 반환 - 수정됨)
	// -----------------------------------------------------------
	@GetMapping("api/pricetabProduct")
	@ResponseBody // JSON 반환을 명시
	public Map<String, Object> priceProductList() { // Model 제거
	    List<Price> prices = priceService.getAllPrice();

	    // 서비스에서 Map<String, Object> 형태로 데이터를 가공하여 반환
	    return priceService.getTableDataFromProducts(prices);
	}

	// 품목 상세정보 조회
	@GetMapping("api/price/getDetail")
	public ResponseEntity<Price> getPrice(@RequestParam String keyword) {
		System.out.println("controller 확인");
		System.out.println("조회 priceGroupCode: " + keyword);

		Price price = priceService.getPriceByPriceGroupCode(keyword);
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
}
