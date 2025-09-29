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

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.service.PartnerService;

@Controller
public class PartnerController {

	private final PartnerService partnerService;

	@Autowired
	public PartnerController(PartnerService partnerService) {
		this.partnerService = partnerService;
	}

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
	@PostMapping("api/registPartner")
	public ResponseEntity<Partner> registPartner(@ModelAttribute Partner partner) {
		Partner saved = partnerService.savePartner(partner);
		return ResponseEntity.ok(saved);
	}

}
