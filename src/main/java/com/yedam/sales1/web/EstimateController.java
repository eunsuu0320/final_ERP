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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import com.yedam.hr.domain.Employee;
import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.EstimateDetail;
import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.dto.EstimateRegistrationDTO;
import com.yedam.sales1.service.EstimateDetailService;
import com.yedam.sales1.service.EstimateService;

@Controller
public class EstimateController {

	private final EstimateService estimateService;
	private final EstimateDetailService estimateDetailService;

	@Autowired
	public EstimateController(EstimateService estimateService, EstimateDetailService estimateDetailService) {
		this.estimateService = estimateService;
		this.estimateDetailService = estimateDetailService;

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

	@GetMapping("api/estimate/getDetail")
	public ResponseEntity<EstimateRegistrationDTO> getProduct(@RequestParam Long keyword) {
		System.out.println("controller 확인");
		System.out.println("조회 estimateUniqueCode: " + keyword);

		// 1. 헤더 및 상세 데이터 조회
		Estimate estimate = estimateService.getEstimateByEstimateUniqueCode(keyword);
		List<EstimateDetail> estimateDetailList = estimateDetailService.getEstimateDetailByEstimateUniqueCode(keyword);

		// [수정 2] 데이터가 없을 경우 처리
		if (estimate == null) {
			return ResponseEntity.notFound().build();
		}

		// 2. DTO 객체 생성 및 데이터 매핑
		EstimateRegistrationDTO dto = new EstimateRegistrationDTO();

		// 2-1. 헤더 데이터 매핑 (Estimate -> DTO)
		// DTO에 정의된 필드에 맞게 Estimate 엔티티의 값을 복사합니다.
		dto.setPartnerCode(estimate.getPartnerCode());
		dto.setPartnerName(estimate.getPartner().getPartnerName());
		dto.setDeliveryDate(estimate.getDeliveryDate());
//		dto.setValidPeriod(estimate.getExpiryDate());
		dto.setRemarks(estimate.getRemarks());
		dto.setManager(estimate.getManager());
		dto.setManagerName(estimate.getManagerEmp().getName());

		// Estimate 엔티티에 PostCode, Address, PayCondition 필드가 있다고 가정하고 매핑합니다.
		// 만약 Estimate 엔티티에 해당 필드가 없다면, 이 부분은 수정하거나 제외해야 합니다.
		dto.setPostCode(estimate.getPostCode());
		dto.setAddress(estimate.getAddress());
		dto.setPayCondition(estimate.getPayCondition());

		// 2-2. 상세 목록 데이터 매핑 (List<EstimateDetail> -> DTO의 detailList)
		dto.setDetailList(estimateDetailList);
		
		if (estimateDetailList != null && !estimateDetailList.isEmpty()) {
		    for (EstimateDetail detail : estimateDetailList) {
		        if (detail.getProduct() != null) {
		            detail.setProductName(detail.getProduct().getProductName());
		            detail.setProductSize(detail.getProduct().getProductSize());
		            detail.setUnit(detail.getProduct().getUnit());
		        }
		    }
		}

		System.out.println(estimateDetailList);

		// [수정 3] 매핑된 DTO를 ResponseEntity.ok()로 감싸서 반환
		return ResponseEntity.ok(dto);
	}

	@PostMapping("api/updateEstimate")
	public ResponseEntity<Map<String, Object>> updateEstimateStatus(@RequestBody Map<String, String> request) {
		try {
			String estimateCode = request.get("estimateCode");
			String status = request.get("status");

			// 필수 파라미터 검증
			if (estimateCode == null || status == null) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "견적서 코드와 상태는 필수입니다."));
			}

			// Service 계층의 상태 업데이트 메서드 호출 (estimateService에 이 메서드가 정의되어 있어야 합니다.)
			boolean updated = estimateService.updateEstimateStatus(estimateCode, status);

			if (updated) {
				// 업데이트 성공 시 클라이언트 (JS)가 기대하는 success: true 반환
				return ResponseEntity.ok(Map.of("success", true, "message", "진행 상태가 성공적으로 변경되었습니다."));
			} else {
				// Service 단에서 업데이트할 대상을 찾지 못했거나 DB 오류가 발생한 경우
				return ResponseEntity.status(400)
						.body(Map.of("success", false, "message", "업데이트할 견적서를 찾을 수 없거나 DB 처리 중 오류가 발생했습니다."));
			}

		} catch (Exception e) {
			// 예외 발생 시 서버 오류 응답 반환
			System.err.println("견적 상태 업데이트 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "message", "서버 내부 오류로 상태 업데이트에 실패했습니다.", "error", e.getMessage()));
		}
	}
	
	
	
	@GetMapping("api/estimate/search")
	// 반환 타입을 Map 리스트로 변경해야 합니다.
	public ResponseEntity<List<Map<String, Object>>> getEstimateSearch(@ModelAttribute Estimate searchVo) {

		System.out.println("조회 조건 Estimate VO: " + searchVo);

		// 1. 기존처럼 필터링된 Product VO 리스트를 가져옵니다. (영문 키)
		List<Estimate> estimates = estimateService.getFilterEstimate(searchVo);

		// 2. ★★★ 핵심 수정: 영문 키 리스트를 한글 키 Map으로 변환합니다. ★★★
		Map<String, Object> tableData = estimateService.getTableDataFromEstimate(estimates);

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
