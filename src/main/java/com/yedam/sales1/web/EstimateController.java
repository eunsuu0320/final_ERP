package com.yedam.sales1.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.dto.EstimateRegistrationDTO;
import com.yedam.sales1.service.EstimateService;

@Controller
public class EstimateController {

	private final EstimateService estimateService;

	@Autowired
	public EstimateController(EstimateService estimateService) {
		this.estimateService = estimateService;
	}

	@GetMapping("estimateList")
	public String estimateList(Model model) {
		List<Estimate> estimate = estimateService.getAllEstimate();

		Map<String, Object> tableData = estimateService.getTableDataFromEstimate(estimate);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/estimateList";
	}

	@PostMapping("api/registEstimate")
	public ResponseEntity<Map<String, Object>> registEstimate(@RequestBody EstimateRegistrationDTO dto) {
		try {
			// Service 계층의 통합 등록 메서드 호출
			Long newId = estimateService.registerNewEstimate(dto);

			// 성공 응답: HTTP 200 OK와 함께 등록된 ID 반환
			return ResponseEntity.ok(Map.of("message", "견적서가 성공적으로 등록되었습니다.", "id", newId));

		} catch (Exception e) {
			// 실패 응답: HTTP 500 Internal Server Error와 함께 오류 메시지 반환
			System.err.println("견적 등록 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500).body(Map.of("message", "견적서 등록 실패", "error", e.getMessage()));
		}
	}

}
