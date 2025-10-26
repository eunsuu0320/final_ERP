package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;
import com.yedam.sales1.domain.Product;

@Repository
public interface PriceRepository extends JpaRepository<Price, Long> {

	@Query("SELECT p FROM Price p " + "WHERE p.companyCode = :companyCode")
	List<Price> findAll(@Param("companyCode") String companyCode);

	@Query("SELECT MAX(p.priceGroupCode) FROM Price p")
	String findMaxPriceGroupCode();

	@Query("SELECT MAX(p.priceUniqueCode) FROM Price p")
	Long findMaxPriceUniqueCode();

	@Query("select p from Price p " + "where p.priceGroupCode = :priceGroupCode " + "AND p.companyCode = :companyCode "
			+ "AND p.isCurrentVersion = 'Y' ")
	Price findByPriceGroupCode(@Param("priceGroupCode") String priceGroupCode,
			@Param("companyCode") String companyCode);

	@Query("SELECT DISTINCT p FROM Price p " + "LEFT JOIN FETCH p.priceDetails pd " + "LEFT JOIN FETCH pd.product "
			+ "LEFT JOIN FETCH pd.partner " + "WHERE p.companyCode = :companyCode "
			+ "ORDER BY p.priceUniqueCode ")
	List<Price> findAllWithAllRelations(@Param("companyCode") String companyCode);

	@Query("SELECT DISTINCT p FROM Price p " + "JOIN FETCH p.priceDetails pd " + "JOIN FETCH pd.product "
			+ "LEFT JOIN FETCH pd.partner " // 공백 추가
			+ "ORDER BY p.priceUniqueCode")
	List<Price> findAllWithProduct(@Param("companyCode") String companyCode);

	@Query("SELECT DISTINCT p FROM Price p " + "JOIN FETCH p.priceDetails pd " + "LEFT JOIN FETCH pd.product "
			+ "JOIN FETCH pd.partner " // 공백 추가
			+ "ORDER BY p.priceUniqueCode")
	List<Price> findAllWithPartner(@Param("companyCode") String companyCode);

	@Query("SELECT pd.partnerCode " + "FROM PriceDetail pd " + "INNER JOIN Price p "
			+ "ON pd.priceUniqueCode = p.priceUniqueCode "
			+ "WHERE pd.partnerCode IS NOT NULL and p.priceUniqueCode = :priceUniqueCode")
	List<String> findPartnerCodes(@Param("priceUniqueCode") Integer priceUniqueCode);

	@Query("SELECT pd.productCode " + "FROM PriceDetail pd " + "INNER JOIN Price p "
			+ "ON pd.priceUniqueCode = p.priceUniqueCode "
			+ "WHERE pd.productCode IS NOT NULL and p.priceUniqueCode = :priceUniqueCode")
	List<String> findProductCodes(@Param("priceUniqueCode") Integer priceUniqueCode);

	@Query("SELECT p FROM Price p "
			+ "WHERE (:#{#searchVo.priceGroupCode} IS NULL OR p.priceGroupCode = :#{#searchVo.priceGroupCode}) "
			+ "AND (:#{#searchVo.priceGroupName} IS NULL OR p.priceGroupName LIKE %:#{#searchVo.priceGroupName}%) "
			+ "AND (:#{#searchVo.validDate} IS NULL OR "
			+ "     (:#{#searchVo.validDate} BETWEEN p.startDate AND p.endDate)) " + "AND p.companyCode = :companyCode")
	List<Price> findByFilter(@Param("searchVo") Price searchVo, @Param("companyCode") String companyCode);

	
    @Query("SELECT p FROM Price p WHERE p.priceUniqueCode IN :codes AND p.usageStatus = 'Y'")
    List<Price> findByPriceUniqueCodeIn(@Param("codes") List<Integer> codes);
	
	
}
