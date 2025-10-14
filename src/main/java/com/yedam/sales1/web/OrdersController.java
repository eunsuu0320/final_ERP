package com.yedam.sales1.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.dto.OrderRegistrationDTO;
import com.yedam.sales1.repository.EstimateRepository;
import com.yedam.sales1.service.OrdersService;

@Controller
public class OrdersController {

	private final OrdersService ordersService;

	@Autowired
	public OrdersController(OrdersService ordersService) {
		this.ordersService = ordersService;
	}

	@GetMapping("ordersList")
	public String ordersList(Model model) {
		List<Orders> orders = ordersService.getAllOrders();

		Map<String, Object> tableData = ordersService.getTableDataFromOrders(orders);

		model.addAttribute("columns", tableData.get("columns"));
		model.addAttribute("rows", tableData.get("rows"));

		return "sales1/ordersList";
	}

	@PostMapping("api/registOrders")
	public ResponseEntity<Map<String, Object>> registOrder(@RequestBody OrderRegistrationDTO dto) {
		try {
			Long newId = ordersService.registerNewOrders(dto);

			return ResponseEntity.ok(Map.of("message", "주문서가 성공적으로 등록되었습니다.", "id", newId));

		} catch (Exception e) {
			System.err.println("주문 등록 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500).body(Map.of("message", "주문서 등록 실패", "error", e.getMessage()));
		}
	}

	@PostMapping("api/updateOrders")
	public ResponseEntity<Map<String, Object>> updateOrdersStatus(@RequestBody Map<String, String> request) {
		try {
			String orderCode = request.get("orderCode");
			String status = request.get("status");

			// 필수 파라미터 검증
			if (orderCode == null || status == null) {
				return ResponseEntity.badRequest().body(Map.of("success", false, "message", "주문서 코드와 상태는 필수입니다."));
			}

			// Service 계층의 상태 업데이트 메서드 호출 (estimateService에 이 메서드가 정의되어 있어야 합니다.)
			boolean updated = ordersService.updateOrdersStatus(orderCode, status);

			if (updated) {
				// 업데이트 성공 시 클라이언트 (JS)가 기대하는 success: true 반환
				return ResponseEntity.ok(Map.of("success", true, "message", "진행 상태가 성공적으로 변경되었습니다."));
			} else {
				// Service 단에서 업데이트할 대상을 찾지 못했거나 DB 오류가 발생한 경우
				return ResponseEntity.status(400)
						.body(Map.of("success", false, "message", "업데이트할 주문서를 찾을 수 없거나 DB 처리 중 오류가 발생했습니다."));
			}

		} catch (Exception e) {
			// 예외 발생 시 서버 오류 응답 반환
			System.err.println("견적 상태 업데이트 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500)
					.body(Map.of("success", false, "message", "서버 내부 오류로 상태 업데이트에 실패했습니다.", "error", e.getMessage()));
		}
	}

}
