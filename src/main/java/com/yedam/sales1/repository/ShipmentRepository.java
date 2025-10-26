package com.yedam.sales1.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employee;
import com.yedam.sales1.domain.Shipment;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

	@Query("SELECT p FROM Shipment p " + "WHERE p.companyCode = :companyCode ")
	List<Shipment> findAll(@Param("companyCode") String companyCode);

	@Modifying
	@Query("UPDATE Shipment s SET s.isInvoice = 'Y' WHERE s.shipmentCode = :shipmentCode")
	void updateIsInvoice(@Param("shipmentCode") String shipmentCode);

	@Query("SELECT MAX(p.shipmentCode) FROM Shipment p")
	String findMaxShipmentCode();

	@Query("SELECT s FROM Shipment s WHERE s.shipmentCode = :shipmentCode ")
	Optional<Shipment> findByShipmentCode(@Param("shipmentCode") String shipmentCode);

	@Query("""
			    select e from Employee e
			    where e.companyCode = :companyCode
			    AND e.dept = '333'
			""")
	List<Employee> findByCompanyCodeSalesEmployee(String companyCode);

	@Query("SELECT p FROM Shipment p "
			+ "WHERE (:#{#searchVo.partnerCode} IS NULL OR p.partnerCode LIKE %:#{#searchVo.partnerCode}%) "
			+ "AND p.companyCode = :companyCode ")
	List<Shipment> findByFilter(@Param("searchVo") Shipment searchVo, @Param("companyCode") String companyCode);

	@Query("""
			    SELECT new map(
			        s.shipmentCode as shipmentCode,
			        s.shipmentDate as shipmentDate,
			        s.totalQuantity as totalQuantity,
			        p.partnerName as partnerName,
			        s.manager as manager,
			        s.warehouse as warehouse
			    )
			    FROM Shipment s
			    JOIN Partner p ON s.partnerCode = p.partnerCode
			    WHERE s.partnerCode = :partnerCode
			      AND s.status = '판매완료'
			      AND (s.isInvoice IS NULL OR s.isInvoice <> 'Y')
			""")
	List<Map<String, Object>> findCompletedShipmentsByPartnerMap(@Param("partnerCode") String partnerCode);

	// ✅ 2. 금액 계산용 (Shipment 엔티티 그대로 반환)
	@Query("""
			    SELECT s
			    FROM Shipment s
			    WHERE s.partnerCode = :partnerCode
			    AND s.status = '출하완료'
			""")
	List<Shipment> findCompletedShipmentsForCalc(@Param("partnerCode") String partnerCode);

	@Query(value = """
			SELECT s.shipment_code AS shipmentCode,
			       s.shipment_date AS shipmentDate,
			       s.partner_code AS partnerCode,
			       p.partner_name AS partnerName,
			       s.manager AS manager,
			       s.warehouse AS warehouse,
			       NVL(SUM(sd.now_quantity), 0) AS totalQuantity
			  FROM shipment s
			  JOIN partner p ON s.partner_code = p.partner_code
			  LEFT JOIN shipment_detail sd ON s.shipment_code = sd.shipment_code
			 WHERE s.partner_code = :partnerCode
			   AND s.status = '출하완료'
			 GROUP BY s.shipment_code, s.shipment_date, s.partner_code, p.partner_name, s.manager, s.warehouse
			 ORDER BY s.shipment_date DESC
			""", nativeQuery = true)
	List<Map<String, Object>> findCompletedShipmentsByPartner(@Param("partnerCode") String partnerCode);

}
