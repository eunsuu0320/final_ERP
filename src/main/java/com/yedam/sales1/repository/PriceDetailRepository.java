package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.PriceDetail;

@Repository
public interface PriceDetailRepository extends JpaRepository<PriceDetail, String> {

	List<PriceDetail> findAll();

	
	void deleteByPriceUniqueCode(Integer PriceUniqueCode);
	
	
	@Query("SELECT MAX(pd.priceDetailCode) FROM PriceDetail pd")
	String findMaxPriceDetailCode();
	
	
	
	void save(List<PriceDetail> newDetails);
	

}