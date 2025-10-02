package com.yedam.sales1.service;

import java.util.List;

import com.yedam.sales1.domain.OrderDetail;

public interface OrderDetailService {
	List<OrderDetail> getAllOrderDetail();

	OrderDetail saveOrderDetail(OrderDetail orderDetail);

}
