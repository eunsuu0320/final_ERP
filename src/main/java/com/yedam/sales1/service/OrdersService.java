package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.dto.OrderRegistrationDTO;

public interface OrdersService {
	List<Orders> getAllOrders();

	Map<String, Object> getTableDataFromOrders(List<Orders> orders);

	Orders saveOrders(Orders orders);
	
	Long registerNewOrders(OrderRegistrationDTO dto);
}
