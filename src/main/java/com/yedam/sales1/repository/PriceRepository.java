package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

	List<Price> findAll();

	@Query("SELECT MAX(p.priceGroupCode) FROM Price p")
	String findMaxPriceGroupCode();

	@Query("SELECT MAX(p.priceUniqueCode) FROM Price p")
	Long findMaxPriceUniqueCode();

	Price findByPriceGroupCode(String priceGroupCode);

	@Query("SELECT DISTINCT p FROM Price p " + "LEFT JOIN FETCH p.priceDetails pd " + "LEFT JOIN FETCH pd.product "
			+ "LEFT JOIN FETCH pd.partner")
	List<Price> findAllWithAllRelations();

	@Query("SELECT DISTINCT p FROM Price p " + "JOIN FETCH p.priceDetails pd " + "JOIN FETCH pd.product "
			+ "LEFT JOIN FETCH pd.partner ")
	List<Price> findAllWithProduct();

	@Query("SELECT DISTINCT p FROM Price p " + "JOIN FETCH p.priceDetails pd " + "LEFT JOIN FETCH pd.product "
			+ "JOIN FETCH pd.partner ")
	List<Price> findAllWithPartner();

	@Query("SELECT pd.partnerCode " + "FROM PriceDetail pd " + "INNER JOIN Price p "
			+ "ON pd.priceUniqueCode = p.priceUniqueCode " + "WHERE pd.partnerCode IS NOT NULL and p.priceUniqueCode = :priceUniqueCode")
	List<String> findPartnerCodes(@Param("priceUniqueCode") Integer priceUniqueCode);
	
	

	@Query("SELECT pd.productCode " + "FROM PriceDetail pd " + "INNER JOIN Price p "
			+ "ON pd.priceUniqueCode = p.priceUniqueCode " + "WHERE pd.productCode IS NOT NULL and p.priceUniqueCode = :priceUniqueCode")
	List<String> findProductCodes(@Param("priceUniqueCode") Integer priceUniqueCode);

}
