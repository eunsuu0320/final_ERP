package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.PriceDetail;

import jakarta.transaction.Transactional;

@Repository
public interface PriceDetailRepository extends JpaRepository<PriceDetail, String> {

	@Query("SELECT od FROM PriceDetail od where od.companyCode = :companyCode")
	List<PriceDetail> findAll(@Param("companyCode") String companyCode);

	void deleteByPriceUniqueCode(Integer PriceUniqueCode);

	@Modifying
	@Transactional
	@Query("DELETE FROM PriceDetail od WHERE od.partnerCode = :partnerCode")
	void deleteByPartnerCode(@Param("partnerCode") String partnerCode);

	@Modifying
	@Transactional
	@Query("DELETE FROM PriceDetail od WHERE od.productCode = :productCode")
	void deleteByProductCode(@Param("productCode") String productCode);

	@Query("SELECT MAX(pd.priceDetailCode) FROM PriceDetail pd")
	String findMaxPriceDetailCode();

	void save(List<PriceDetail> newDetails);

	// 거래처로 매칭되는 단가(거래처단가)
	@Query("""
			    SELECT pd
			    FROM PriceDetail pd
			    JOIN FETCH pd.price p
			    WHERE pd.partnerCode = :partnerCode
			""")
	List<PriceDetail> findAllByPartnerCodeFetch(@Param("partnerCode") String partnerCode);

	// 품목으로 매칭되는 단가(품목단가)
	@Query("""
			    SELECT pd
			    FROM PriceDetail pd
			    JOIN FETCH pd.price p
			    WHERE pd.productCode IN :productCodes
			""")
	List<PriceDetail> findAllByProductCodeInFetch(@Param("productCodes") List<String> productCodes);

	// (연관관계 fetch 없이도 쓰고 싶다면 아래 기본 버전도 필요시 유지)
	List<PriceDetail> findByPartnerCode(String partnerCode);

	List<PriceDetail> findByProductCodeIn(List<String> productCodes);

}