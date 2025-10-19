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

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.dto.OrderModalDTO;
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
            if (orderCode == null || status == null) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "주문서 코드와 상태는 필수입니다."));
            }
            boolean updated = ordersService.updateOrdersStatus(orderCode, status);
            if (updated) {
                return ResponseEntity.ok(Map.of("success", true, "message", "진행 상태가 성공적으로 변경되었습니다."));
            } else {
                return ResponseEntity.status(400).body(Map.of("success", false, "message", "업데이트 대상 없음 또는 DB 오류"));
            }
        } catch (Exception e) {
            System.err.println("주문 상태 업데이트 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("success", false, "message", "서버 내부 오류", "error", e.getMessage()));
        }
    }

    // ✅ 검색 (경로 통일: /api/orders/search) - JS에서 이걸 더이상 쓰진 않지만 호환 유지
    @GetMapping("api/orders/search")
    public ResponseEntity<List<Map<String, Object>>> getOrderSearch(@ModelAttribute Orders searchVo) {
        List<Orders> orders = ordersService.getFilterOrder(searchVo);
        Map<String, Object> tableData = ordersService.getTableDataFromOrders(orders);
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) tableData.get("rows");
        if (rows != null && !rows.isEmpty()) return ResponseEntity.ok(rows);
        return ResponseEntity.ok(Collections.emptyList());
    }

    // ✅ 상세 조회 (요구사항 5): 주문서 고유코드로 헤더+디테일 DTO 반환
    @GetMapping("api/orders/getDetail")
    public ResponseEntity<OrderModalDTO> getOrderDetail(@RequestParam Long keyword) {
        OrderModalDTO dto = ordersService.getOrderModalByOrderUniqueCode(keyword);
        if (dto == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(dto);
    }
}
