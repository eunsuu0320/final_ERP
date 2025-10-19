package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.dto.OrderModalDTO;
import com.yedam.sales1.dto.OrderRegistrationDTO;

public interface OrdersService {
    List<Orders> getAllOrders();

    List<Orders> getFilterOrder(Orders searchVo);

    Map<String, Object> getTableDataFromOrders(List<Orders> orders);

    Long registerNewOrders(OrderRegistrationDTO dto);

    boolean updateOrdersStatus(String orderCode, String status);

    // 상세 모달용 DTO
    OrderModalDTO getOrderModalByOrderUniqueCode(Long orderUniqueCode);
}
