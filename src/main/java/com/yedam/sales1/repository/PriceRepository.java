package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;

@Repository
public interface PriceRepository extends
		JpaRepository<Price, Long>{

	List<Price> findAll();
	
	@Query("SELECT MAX(p.priceGroupCode) FROM Price p")
	String findMaxPriceGroupCode();
	
	@Query("SELECT MAX(p.priceUniqueCode) FROM Price p")
	Long findMaxPriceUniqueCode();

	Price findByPriceGroupCode(String priceGroupCode);
	
//	@Query("SELECT p FROM Price p " +
//		       "WHERE (:productName IS NULL OR p.productName = :productName) " +
//		       "AND (:productGroup IS NULL OR p.productGroup = :productGroup) " +
//		       "AND (:warehouseCode IS NULL OR p.warehouseCode = :warehouseCode)")
//		List<Partner> findByFilter(
//		        @Param("productName") String productName,
//		        @Param("productGroup") String productGroup,
//		        @Param("warehouseCode") String warehouseCode);

}
