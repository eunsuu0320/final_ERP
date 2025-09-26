package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Shipment;

public interface ShipmentService {
	List<Shipment> getAllShipment();

	Map<String, Object> getTableDataFromShipments(List<Shipment> shipments);
	
	Shipment saveShipment(Shipment shipments);
}
