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
import org.springframework.web.bind.annotation.RequestBody;

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.dto.EstimateRegistrationDTO;
import com.yedam.sales1.dto.OrderRegistrationDTO;
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
			// Service 계층의 통합 등록 메서드 호출
			Long newId = ordersService.registerNewOrders(dto);

			// 성공 응답: HTTP 200 OK와 함께 등록된 ID 반환
			return ResponseEntity.ok(Map.of("message", "주문서가 성공적으로 등록되었습니다.", "id", newId));

		} catch (Exception e) {
			System.err.println("주문 등록 중 오류 발생: " + e.getMessage());
			return ResponseEntity.status(500).body(Map.of("message", "주문서 등록 실패", "error", e.getMessage()));
		}
	}
    
}
