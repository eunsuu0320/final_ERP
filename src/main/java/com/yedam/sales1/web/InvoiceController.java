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

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.dto.EstimateRegistrationDTO;
import com.yedam.sales1.dto.InvoiceRegistrationDTO;
import com.yedam.sales1.service.InvoiceService;

@Controller
public class InvoiceController {

	
    private final InvoiceService invoiceService;

    @Autowired
    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @GetMapping("invoiceList")
    public String invoiceList(Model model) {
        List<Invoice> invoice = invoiceService.getAllInvoice();
        
        Map<String, Object> tableData = invoiceService.getTableDataFromInvoice(invoice);

        model.addAttribute("columns", tableData.get("columns"));
        model.addAttribute("rows", tableData.get("rows"));

        return "sales1/invoiceList";
    }
    
    
    // 품목 등록
    @PostMapping("api/registInvoice")
    public ResponseEntity<Map<String, Object>> registOrders(@RequestBody InvoiceRegistrationDTO dto) {
		try {
			// Service 계층의 통합 등록 메서드 호출
			Long newId = invoiceService.registerNewInvoice(dto);

			// 성공 응답: HTTP 200 OK와 함께 등록된 ID 반환
			return ResponseEntity.ok(Map.of("message", "견적서가 성공적으로 등록되었습니다.", "id", newId));

		} catch (Exception e) {
			// 실패 응답: HTTP 500 Internal Server Error와 함께 오류 메시지 반환
			System.err.println("견적 등록 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500).body(Map.of("message", "견적서 등록 실패", "error", e.getMessage()));
		}
    }
    
    
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
				return ResponseEntity.status(400).body(Map.of("success", false, "message", "업데이트할 청구서를 찾을 수 없거나 DB 처리 중 오류가 발생했습니다."));
			}

		} catch (Exception e) {
			// 예외 발생 시 서버 오류 응답 반환
			System.err.println("청구서 상태 업데이트 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500).body(Map.of("success", false, "message", "서버 내부 오류로 상태 업데이트에 실패했습니다.", "error", e.getMessage()));
		}
	}
	
	
	@GetMapping("api/invoice/search")
	// 반환 타입을 Map 리스트로 변경해야 합니다.
	public ResponseEntity<List<Map<String, Object>>> getInvoiceSearch(@ModelAttribute Invoice searchVo) {

		System.out.println("조회 조건 Invoice VO: " + searchVo);

		// 1. 기존처럼 필터링된 Product VO 리스트를 가져옵니다. (영문 키)
		List<Invoice> invoices = invoiceService.getFilterInvoice(searchVo);

		// 2. ★★★ 핵심 수정: 영문 키 리스트를 한글 키 Map으로 변환합니다. ★★★
		Map<String, Object> tableData = invoiceService.getTableDataFromInvoice(invoices);

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
