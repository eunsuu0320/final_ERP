package com.yedam.sales1.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employee;
import com.yedam.sales1.domain.Shipment;

@Repository
public interface ShipmentRepository extends
		JpaRepository<Shipment, String>{

	List<Shipment> findAll();
	
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
}
