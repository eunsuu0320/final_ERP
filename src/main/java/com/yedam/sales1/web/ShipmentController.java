package com.yedam.sales1.web;

import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.sales1.domain.Shipment;
import com.yedam.sales1.dto.ShipmentRegistrationDTO;
import com.yedam.sales1.repository.ShipmentRepository;
import com.yedam.sales1.service.ShipmentService;

@Controller
public class ShipmentController {

	private final ShipmentService shipmentService;
	private final ShipmentRepository shipmentRepository;

	@Autowired
	public ShipmentController(ShipmentService shipmentService, ShipmentRepository shipmentRepository) {
		this.shipmentService = shipmentService;
		this.shipmentRepository = shipmentRepository;

	}

	@GetMapping("shipmentList")
	public String shipmentList(Model model) {
		List<Shipment> shipments = shipmentService.getAllShipment();

		Map<String, Object> tableData = shipmentService.getTableDataFromShipments(shipments);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/shipmentList";
	}

	@PostMapping("api/registShipment")
	public ResponseEntity<Map<String, Object>> registShipment(@RequestBody ShipmentRegistrationDTO dto) {
		try {
			// Service 계층의 통합 등록 메서드 호출
			// Shipment의 PK는 String 타입의 shipmentCode이므로 반환 타입이 String입니다.
			String newShipmentCode = shipmentService.registerNewShipment(dto); // ⭐ 메서드명 및 반환 타입 변경

			// 성공 응답: HTTP 200 OK와 함께 등록된 출하 코드(Code) 반환
			return ResponseEntity.ok(Map.of("message", "출하 지시가 성공적으로 등록되었습니다.", "id", newShipmentCode)); // ⭐ 메시지 및 ID
																											// 변경

		} catch (Exception e) {
			// 실패 응답: HTTP 500 Internal Server Error와 함께 오류 메시지 반환
			System.err.println("출하 등록 중 오류 발생: " + e.getMessage()); // ⭐ 메시지 변경
			return ResponseEntity.status(500).body(Map.of("message", "출하 등록 실패", "error", e.getMessage())); // ⭐ 메시지 변경
		}
	}

	@GetMapping("api/shipment/getDetail")
	@ResponseBody
	public ResponseEntity<?> getShipmentDetail(@RequestParam String keyword) {
		// keyword = "SHP0005"
		System.out.println("getDetail keyword: " + keyword);

		Shipment shipment = shipmentRepository.findByShipmentCode(keyword)
				.orElseThrow(() -> new RuntimeException("출하 정보 없음"));

		return ResponseEntity.ok(shipment);
	}

	@GetMapping("/api/shipment/completed")
	public ResponseEntity<List<Map<String, Object>>> getCompletedShipments(@RequestParam String partnerCode) {
		try {
			List<Map<String, Object>> shipments = shipmentRepository.findCompletedShipmentsByPartnerMap(partnerCode);

			if (shipments == null || shipments.isEmpty()) {
				return ResponseEntity.ok(Collections.emptyList());
			}

			return ResponseEntity.ok(shipments);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500)
					.body(Collections.singletonList(Map.of("error", "출하지시서 데이터 조회 중 오류 발생: " + e.getMessage())));
		}
	}

	@PostMapping("api/updateShipment")
	public ResponseEntity<Map<String, Object>> updateShipmentStatus(@RequestBody Map<String, String> request) {
		try {
			String shipmentCode = request.get("shipmentCode");
			String status = request.get("status");

			// 필수 파라미터 검증
			if (shipmentCode == null || status == null) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "출하지시서 코드와 상태는 필수입니다."));
			}

			// Service 계층의 상태 업데이트 메서드 호출 (estimateService에 이 메서드가 정의되어 있어야 합니다.)
			boolean updated = shipmentService.updateShipmentStatus(shipmentCode, status);

			if (updated) {
				// 업데이트 성공 시 클라이언트 (JS)가 기대하는 success: true 반환
				return ResponseEntity.ok(Map.of("success", true, "message", "진행 상태가 성공적으로 변경되었습니다."));
			} else {
				// Service 단에서 업데이트할 대상을 찾지 못했거나 DB 오류가 발생한 경우
				return ResponseEntity.status(400)
						.body(Map.of("success", false, "message", "업데이트할 출하지시서를 찾을 수 없거나 DB 처리 중 오류가 발생했습니다."));
			}

		} catch (Exception e) {
			// 예외 발생 시 서버 오류 응답 반환
			System.err.println("출하지시서 상태 업데이트 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "message", "서버 내부 오류로 상태 업데이트에 실패했습니다.", "error", e.getMessage()));
		}
	}

	@GetMapping("api/shipment/search")
	// 반환 타입을 Map 리스트로 변경해야 합니다.
	public ResponseEntity<List<Map<String, Object>>> getShipmentSearch(@ModelAttribute Shipment searchVo) {

		System.out.println("조회 조건 Shipment VO: " + searchVo);

		// 1. 기존처럼 필터링된 Product VO 리스트를 가져옵니다. (영문 키)
		List<Shipment> shpiments = shipmentService.getFilterShipment(searchVo);

		// 2. ★★★ 핵심 수정: 영문 키 리스트를 한글 키 Map으로 변환합니다. ★★★
		Map<String, Object> tableData = shipmentService.getTableDataFromShipments(shpiments);

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
