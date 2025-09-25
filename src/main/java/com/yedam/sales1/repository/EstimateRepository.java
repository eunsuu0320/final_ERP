package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.Partner;

@Repository
public interface EstimateRepository extends
		JpaRepository<Estimate, Long>{

	List<Estimate> findAll();
	
	@Query("SELECT MAX(p.estimateCode) FROM Estimate p")
	String findMaxEstimateCode();
	
//	@Query("SELECT p FROM Estimate p " +
//		       "WHERE (:productName IS NULL OR p.productName = :productName) " +
//		       "AND (:productGroup IS NULL OR p.productGroup = :productGroup) " +
//		       "AND (:warehouseCode IS NULL OR p.warehouseCode = :warehouseCode)")
//		List<Estimate> findByFilter(
//		        @Param("productName") String productName,
//		        @Param("productGroup") String productGroup,
//		        @Param("warehouseCode") String warehouseCode);

}
