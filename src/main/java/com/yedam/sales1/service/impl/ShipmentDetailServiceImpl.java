package com.yedam.sales1.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.ShipmentDetail;
import com.yedam.sales1.repository.ShipmentDetailRepository;
import com.yedam.sales1.service.ShipmentDetailService;

@Service
public class ShipmentDetailServiceImpl implements ShipmentDetailService {

    private final ShipmentDetailRepository shipmentDetailRepository;

    @Autowired
    public ShipmentDetailServiceImpl(ShipmentDetailRepository shipmentDetailRepository) {
        this.shipmentDetailRepository = shipmentDetailRepository;
    }

    @Override
    public List<ShipmentDetail> getAllOrderDetail() {
        return shipmentDetailRepository.findAll();
    }

    @Override
    public ShipmentDetail saveShipmentDetail(ShipmentDetail shipmentDetail) {
        return shipmentDetailRepository.save(shipmentDetail);
    }
}
