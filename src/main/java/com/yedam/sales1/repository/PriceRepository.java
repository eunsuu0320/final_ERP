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
	
	@Query("SELECT DISTINCT p FROM Price p " +
			   "LEFT JOIN FETCH p.priceDetails pd " +
			   "LEFT JOIN FETCH pd.product " +
			   "LEFT JOIN FETCH pd.partner")
		List<Price> findAllWithAllRelations();

}
