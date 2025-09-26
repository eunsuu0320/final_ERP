package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Shipment;
import com.yedam.sales1.repository.ShipmentRepository;
import com.yedam.sales1.service.ShipmentService;

import jakarta.transaction.Transactional;

@Service
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;

    @Autowired
    public ShipmentServiceImpl(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @Override
    public List<Shipment> getAllShipment() {
        return shipmentRepository.findAll();
    }

    @Override
    public Map<String, Object> getTableDataFromShipments(List<Shipment> shipments) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!shipments.isEmpty()) {
            // 컬럼 정의
            columns.add("출하지시서코드");
            columns.add("출하예정일자");
            columns.add("거래처명");
            columns.add("창고명");
            columns.add("품목명");
            columns.add("수량합계");
            columns.add("비고");
            columns.add("진행상태");

            for (Shipment shipment : shipments) {
                Map<String, Object> row = new HashMap<>();
                row.put("출하지시서코드", shipment.getShipmentCode());
                row.put("출하예정일자", shipment.getShipmentDate());
                row.put("거래처명", shipment.getPartnerCode());
                row.put("창고명", shipment.getWarehouse());
                row.put("품목명", shipment.getShipmentCode());
                row.put("수량합계", shipment.getTotalQuantity());
                row.put("비고", shipment.getRemarks());
                row.put("진행상태", shipment.getStatus());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Shipment saveShipment(Shipment shipment) {
        return shipmentRepository.save(shipment);
    }


}
