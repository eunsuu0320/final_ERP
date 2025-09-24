package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Product;

@Repository
public interface PartnerRepository extends
		JpaRepository<Partner, String>{

	List<Partner> findAll();
	
	@Query("SELECT MAX(p.productCode) FROM Product p")
	String findMaxPartnerCode();
	
	@Query("SELECT p FROM Product p " +
		       "WHERE (:productName IS NULL OR p.productName = :productName) " +
		       "AND (:productGroup IS NULL OR p.productGroup = :productGroup) " +
		       "AND (:warehouseCode IS NULL OR p.warehouseCode = :warehouseCode)")
		List<Partner> findByFilter(
		        @Param("productName") String productName,
		        @Param("productGroup") String productGroup,
		        @Param("warehouseCode") String warehouseCode);

}
