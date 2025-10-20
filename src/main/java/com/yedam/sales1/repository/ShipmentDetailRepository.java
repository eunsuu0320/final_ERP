package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.ShipmentDetail;

@Repository
public interface ShipmentDetailRepository extends JpaRepository<ShipmentDetail, String> {

	List<ShipmentDetail> findAll();

	List<ShipmentDetail> findByShipmentCode(String shipmentCode);

	@Query("SELECT MAX(sd.shipmentDetailCode) FROM ShipmentDetail sd")
	String findMaxShipmentDetailCode();

	@Query("""
			    SELECT SUM(od.price * sd.nowQuantity)
			    FROM ShipmentDetail sd
			    JOIN OrderDetail od ON sd.orderDetailCode = od.orderDetailCode
			    WHERE sd.shipmentCode = :shipmentCode
			""")
	Double calcShipmentAmount(@Param("shipmentCode") String shipmentCode);



	@Query(value = """
			SELECT SUM(NVL(od.price, 0) * NVL(sd.now_quantity, 0))
			  FROM shipment_detail sd
			  JOIN order_detail od ON sd.order_detail_code = od.order_detail_code
			 WHERE sd.shipment_code = :shipmentCode
			""", nativeQuery = true)
	Double calcShipmentAmountByShipment(@Param("shipmentCode") String shipmentCode);
	
	
	@Query("""
		    SELECT DISTINCT p.productName
		    FROM ShipmentDetail d
		    JOIN Product p ON d.productCode = p.productCode
		    WHERE d.shipmentCode = :shipmentCode
		    """)
		List<String> findProductNamesByShipmentCode(@Param("shipmentCode") String shipmentCode);

}
