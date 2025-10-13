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

import com.yedam.sales1.domain.Shipment;
import com.yedam.sales1.dto.ShipmentRegistrationDTO;
import com.yedam.sales1.service.ShipmentService;

@Controller
public class ShipmentController {

	private final ShipmentService shipmentService;

	@Autowired
	public ShipmentController(ShipmentService shipmentService) {
		this.shipmentService = shipmentService;
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

}
