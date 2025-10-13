package com.yedam.sales1.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.ShipmentDetail; // ⭐ ShipmentDetail 엔티티로 변경
import com.yedam.sales1.repository.ShipmentDetailRepository; // ⭐ ShipmentDetailRepository로 변경
import com.yedam.sales1.service.ShipmentDetailService; // ⭐ ShipmentDetailService 인터페이스로 변경

@Service
// ⭐ 구현하는 인터페이스 이름을 ShipmentDetailService로 변경
public class ShipmentDetailServiceImpl implements ShipmentDetailService {

	// ⭐ 사용하는 Repository 타입을 ShipmentDetailRepository로 변경
	private final ShipmentDetailRepository shipmentDetailRepository;

	@Autowired
	// ⭐ 생성자 매개변수 타입을 ShipmentDetailRepository로 변경
	public ShipmentDetailServiceImpl(ShipmentDetailRepository shipmentDetailRepository) {
		this.shipmentDetailRepository = shipmentDetailRepository;
	}

    // ShipmentDetailService 인터페이스의 메서드를 구현합니다.
    
	@Override
	// ⭐ 반환 및 매개변수 타입을 ShipmentDetail 엔티티로 변경
	public List<ShipmentDetail> getAllOrderDetail() { 
		// 메서드 이름이 'OrderDetail'로 되어 있어 일관성을 위해 'getAllShipmentDetail'로 변경하는 것이 좋지만,
        // 인터페이스 정의를 따르기 위해 현재는 그대로 둡니다.
		return shipmentDetailRepository.findAll(); 
	}

	@Override
	// ⭐ 반환 및 매개변수 타입을 ShipmentDetail 엔티티로 변경
	public ShipmentDetail saveShipmentDetail(ShipmentDetail shipmentDetail) {
		return shipmentDetailRepository.save(shipmentDetail);
	}


}