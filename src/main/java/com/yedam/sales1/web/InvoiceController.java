package com.yedam.sales1.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yedam.common.ScreenPerm;
import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.dto.InvoiceResponseDto;
import com.yedam.sales1.dto.InvoiceSaveRequestDto;
import com.yedam.sales1.repository.ShipmentDetailRepository;
import com.yedam.sales1.repository.ShipmentRepository;
import com.yedam.sales1.service.InvoiceService;

@Controller
public class InvoiceController {

	private final InvoiceService invoiceService;
	private final ShipmentDetailRepository shipmentDetailRepository;
	private final ShipmentRepository shipmentRepository;

	@Autowired
	public InvoiceController(InvoiceService invoiceService, ShipmentDetailRepository shipmentDetailRepository,
			ShipmentRepository shipmentRepository) {
		this.invoiceService = invoiceService;
		this.shipmentDetailRepository = shipmentDetailRepository;
		this.shipmentRepository = shipmentRepository;
	}

	@ScreenPerm(screen = "SAL_BILL", action = ScreenPerm.Action.READ)
	@GetMapping("invoiceList")
	public String invoiceList(Model model) {
		List<Invoice> invoice = invoiceService.getAllInvoice();

		Map<String, Object> tableData = invoiceService.getTableDataFromInvoice(invoice);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/invoiceList";
	}

	// 품목 등록
	@ScreenPerm(screen = "SAL_BILL", action = ScreenPerm.Action.CREATE)
	@PostMapping("api/registInvoice")
	public ResponseEntity<?> saveInvoice(@RequestBody InvoiceSaveRequestDto dto) {
		invoiceService.saveInvoice(dto);
		return ResponseEntity.ok("등록 완료");
	}

	@GetMapping("api/getDetailInvoice/{invoiceCode}")
	public ResponseEntity<InvoiceResponseDto> getInvoiceDetail(@PathVariable String invoiceCode) {
		InvoiceResponseDto result = invoiceService.getInvoiceDetail(invoiceCode);
		return ResponseEntity.ok(result);
	}

	@ScreenPerm(screen = "SAL_BILL", action = ScreenPerm.Action.UPDATE)
	@PostMapping("api/updateInvoice")
	public ResponseEntity<Map<String, Object>> updateInvoiceStatus(@RequestBody Map<String, String> request) {
		try {
			String invoiceCode = request.get("invoiceCode");
			String status = request.get("status");

			// 필수 파라미터 검증
			if (invoiceCode == null || status == null) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "청구서 코드와 상태는 필수입니다."));
			}

			// Service 계층의 상태 업데이트 메서드 호출 (estimateService에 이 메서드가 정의되어 있어야 합니다.)
			boolean updated = invoiceService.updateInvoiceStatus(invoiceCode, status);

			if (updated) {
				// 업데이트 성공 시 클라이언트 (JS)가 기대하는 success: true 반환
				return ResponseEntity.ok(Map.of("success", true, "message", "진행 상태가 성공적으로 변경되었습니다."));
			} else {
				// Service 단에서 업데이트할 대상을 찾지 못했거나 DB 오류가 발생한 경우
				return ResponseEntity.status(400)
						.body(Map.of("success", false, "message", "업데이트할 청구서를 찾을 수 없거나 DB 처리 중 오류가 발생했습니다."));
			}

		} catch (Exception e) {
			// 예외 발생 시 서버 오류 응답 반환
			System.err.println("청구서 상태 업데이트 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "message", "서버 내부 오류로 상태 업데이트에 실패했습니다.", "error", e.getMessage()));
		}
	}

	/**
	 * ✅ 각 출하지시서별 금액 계산 API shipment_detail × order_detail.price 로 공급가액 계산 부가세 10% 및
	 * 총액까지 반환
	 */
	@GetMapping("/api/invoice/calcAmount")
	public ResponseEntity<Map<String, Object>> calcInvoiceAmount(@RequestParam String shipmentCode) {
		try {
			// 1️⃣ 출하지시서 코드 기준으로 금액 계산 (조인 수행)
			Double supplyAmount = shipmentDetailRepository.calcShipmentAmountByShipment(shipmentCode);
			if (supplyAmount == null)
				supplyAmount = 0.0;

			// 2️⃣ 부가세 및 총액 계산
			double taxAmount = Math.round(supplyAmount * 0.1);
			double totalAmount = supplyAmount + taxAmount;

			Map<String, Object> result = new HashMap<>();
			result.put("supplyAmount", supplyAmount);
			result.put("taxAmount", taxAmount);
			result.put("totalAmount", totalAmount);

			return ResponseEntity.ok(result);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(500).body(Map.of("error", "금액 계산 오류: " + e.getMessage()));
		}
	}

	@ScreenPerm(screen = "SAL_BILL", action = ScreenPerm.Action.UPDATE)
	@PostMapping("/api/updateInvoiceStatus")
	@ResponseBody
	public ResponseEntity<?> updateInvoiceStatus(@RequestBody List<Map<String, Object>> invoices) {
		for (Map<String, Object> invoice : invoices) {
			String code = (String) invoice.get("invoiceCode");
			String status = (String) invoice.get("status");
			invoiceService.updateInvoiceStatus(code, status);
		}
		return ResponseEntity.ok().build();
	}

}
