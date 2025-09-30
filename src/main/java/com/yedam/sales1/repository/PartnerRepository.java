package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Partner;

@Repository
public interface PartnerRepository extends
		JpaRepository<Partner, String>{

	List<Partner> findAll();
	
	@Query("SELECT MAX(p.partnerCode) FROM Partner p")
	String findMaxPartnerCode();

	Partner findByPartnerCode(String partnerCode);
	
//	@Query("SELECT p FROM Partner p " +
//		       "WHERE (:partnerName IS NULL OR p.partnerName = :partnerName) ")
//		List<Partner> findByFilter(
//		        @Param("productName") String productName);

}
