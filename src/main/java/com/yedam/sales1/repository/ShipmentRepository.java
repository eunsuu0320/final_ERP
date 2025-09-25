package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Shipment;

@Repository
public interface ShipmentRepository extends
		JpaRepository<Shipment, String>{

	List<Shipment> findAll();
	
	@Query("SELECT MAX(p.shipmentCode) FROM Shipment p")
	String findMaxShipmentCode();
	
//	@Query("SELECT p FROM Shipment p " +
//		       "WHERE (:productName IS NULL OR p.productName = :productName) " +
//		       "AND (:productGroup IS NULL OR p.productGroup = :productGroup) " +
//		       "AND (:warehouseCode IS NULL OR p.warehouseCode = :warehouseCode)")
//		List<Partner> findByFilter(
//		        @Param("productName") String productName,
//		        @Param("productGroup") String productGroup,
//		        @Param("warehouseCode") String warehouseCode);

}
