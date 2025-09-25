package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.repository.OrdersRepository;
import com.yedam.sales1.service.OrdersService;

import jakarta.transaction.Transactional;

@Service
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;

    @Autowired
    public OrdersServiceImpl(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @Override
    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    @Override
    public Map<String, Object> getTableDataFromOrders(List<Orders> orders) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!orders.isEmpty()) {
            // 컬럼 정의
            columns.add("주문서코드");
            columns.add("등록일자");
            columns.add("거래처명");
            columns.add("담당자");
            columns.add("품목명");
            columns.add("납기일자");
            columns.add("주문금액합계");
            columns.add("견적서코드");
            columns.add("진행상태");

            for (Orders order : orders) {
                Map<String, Object> row = new HashMap<>();
                row.put("주문서코드", order.getOrderCode());
                row.put("등록일자", order.getCreateDate());
                row.put("거래처명", order.getPartnerCode());
                row.put("담당자", order.getManager());
                row.put("품목명", order.getTotalAmount());
                row.put("납기일자", order.getDeliveryDate());
                row.put("주문금액합계", order.getTotalAmount());
                row.put("견적서코드", order.getEstimateUniqueCode());
                row.put("진행상태", order.getStatus());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Orders saveOrders(Orders orders) {
        return ordersRepository.save(orders);
    }


}
