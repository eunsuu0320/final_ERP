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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.yedam.common.ScreenPerm;
import com.yedam.sales1.domain.Loan;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Payment;
import com.yedam.sales1.dto.PartnerRegistrationDTO;
import com.yedam.sales1.service.LoanService;
import com.yedam.sales1.service.PartnerService;
import com.yedam.sales1.service.PaymentService;

@Controller
public class PartnerController {

	private final PartnerService partnerService;
	private final LoanService loanService;
	private final PaymentService paymentService;



	@Autowired
	public PartnerController(PartnerService partnerService, PaymentService paymentService, LoanService loanService) {
		this.partnerService = partnerService;
		this.paymentService = paymentService;
		this.loanService = loanService;

	}

	
	@ScreenPerm(screen = "SAL_CUST", action = ScreenPerm.Action.READ)
	@GetMapping("partnerList")
	public String partnerList(Model model) {
		List<Partner> partners = partnerService.getAllPartner();

		Map<String, Object> tableData = partnerService.getTableDataFromPartners(partners);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/partnerList";
	}

	// 품목 상세정보 조회
	@GetMapping("api/partner/getDetail")
	public ResponseEntity<Partner> getPartner(@RequestParam String keyword) {
		System.out.println("controller 확인");
		System.out.println("조회 partnerCode: " + keyword);

		Partner partner = partnerService.getPartnerByPartnerCode(keyword);
		if (partner != null) {
			return ResponseEntity.ok(partner);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	// 품목 등록
	@ScreenPerm(screen = "SAL_CUST", action = ScreenPerm.Action.CREATE)
	@PostMapping("api/registFullPartner")
	public ResponseEntity<Partner> registFullPartner(@RequestBody PartnerRegistrationDTO partnerData) {
		System.out.println(partnerData);
		Partner savedPartner = partnerService.saveFullPartnerData(partnerData);

		return ResponseEntity.ok(savedPartner);
	}
	
	
	@GetMapping("api/partner/search")
	// 반환 타입을 Map 리스트로 변경해야 합니다.
	public ResponseEntity<List<Map<String, Object>>> getPartnerSearch(@ModelAttribute Partner searchVo) {

		System.out.println("조회 조건 Partner VO: " + searchVo);

		// 1. 기존처럼 필터링된 Product VO 리스트를 가져옵니다. (영문 키)
		List<Partner> partners = partnerService.getFilterPartner(searchVo);

		// 2. ★★★ 핵심 수정: 영문 키 리스트를 한글 키 Map으로 변환합니다. ★★★
		Map<String, Object> tableData = partnerService.getTableDataFromPartners(partners);

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
	
	
	
    // 거래처 상세 조회
    @GetMapping("api/partner/detail/{partnerCode}")
    public Partner getPartnerDetail(@PathVariable String partnerCode) {
        return partnerService.findPartnerDetail(partnerCode);
    }
    
    // 여신(단가) 상세 조회
    @GetMapping("api/loan/detail/{partnerCode}")
    public Loan getLoanDetail(@PathVariable String partnerCode) {
        return loanService.findLoanDetailByPartner(partnerCode);
    }

    // 결제정보 상세조회
    @GetMapping("api/payment/detail/{partnerCode}")
    public List<Payment> getPaymentDetail(@PathVariable String partnerCode) {
        return paymentService.findPaymentsByPartnerCode(partnerCode);
    }
}
