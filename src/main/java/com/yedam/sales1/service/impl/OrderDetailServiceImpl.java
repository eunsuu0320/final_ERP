package com.yedam.sales1.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.OrderDetail;
import com.yedam.sales1.repository.OrderDetailRepository;
import com.yedam.sales1.service.OrderDetailService;

@Service
public class OrderDetailServiceImpl implements OrderDetailService {

	private final OrderDetailRepository orderDetailRepository;

	@Autowired
	public OrderDetailServiceImpl(OrderDetailRepository orderDetailRepository) {
		this.orderDetailRepository = orderDetailRepository;
	}


	@Override
	public List<OrderDetail> getAllOrderDetail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OrderDetail saveOrderDetail(OrderDetail orderDetail) {
		return orderDetailRepository.save(orderDetail);

	}


}
