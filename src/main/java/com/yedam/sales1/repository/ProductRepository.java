package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.hr.domain.Employee;
import com.yedam.sales1.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

	@Query("SELECT p FROM Product p " + "WHERE p.companyCode = :companyCode " + "And p.usageStatus = 'Y' ")
	List<Product> findAll(@Param("companyCode") String companyCode);

	// 회사코드별 사원 조회
	@Query("""
			    select p from Product p
			    where p.companyCode = :companyCode
			""")
	List<Product> findByCompanyCode(String companyCode);

	@Query(value = "SELECT MAX(CAST(SUBSTRING(product_code, 2) AS UNSIGNED)) FROM tbl_product", nativeQuery = true)
	Integer findMaxProductNumber();

	@Query("SELECT p FROM Product p "
			+ "WHERE (:#{#searchVo.productCode} IS NULL OR p.productCode = :#{#searchVo.productCode}) "
			+ "AND (:#{#searchVo.productName} IS NULL OR p.productName LIKE %:#{#searchVo.productName}%) "
			+ "AND (:#{#searchVo.productGroup} IS NULL OR p.productGroup = :#{#searchVo.productGroup}) "
			+ "AND (:#{#searchVo.warehouseCode} IS NULL OR p.warehouseCode = :#{#searchVo.warehouseCode}) "
			+ "And p.usageStatus = 'Y' " + "And p.companyCode = :companyCode ")
	List<Product> findByFilter(@Param("searchVo") Product searchVo, @Param("companyCode") String companyCode);

	Product findByProductCode(String productCode);

}
