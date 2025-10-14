package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Shipment;
import com.yedam.sales1.dto.ShipmentRegistrationDTO; // DTO 임포트 추가

public interface ShipmentService {
	List<Shipment> getAllShipment();

	Map<String, Object> getTableDataFromShipments(List<Shipment> shipments);
	
	Shipment saveShipment(Shipment shipments);
	

	String registerNewShipment(ShipmentRegistrationDTO dto); 
	
	
	boolean updateShipmentStatus(String shipmentCode, String status);

}