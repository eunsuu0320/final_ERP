package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales1.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {

	List<Product> findAll();

	@Query("SELECT MAX(p.productCode) FROM Product p")
	String findMaxProductCode();

    @Query("SELECT p FROM Product p "
            + "WHERE (:#{#searchVo.productName} IS NULL OR p.productName LIKE %:#{#searchVo.productName}%) "
            + "AND (:#{#searchVo.productGroup} IS NULL OR p.productGroup = :#{#searchVo.productGroup}) "
            + "AND (:#{#searchVo.warehouseCode} IS NULL OR p.warehouseCode = :#{#searchVo.warehouseCode})")
    List<Product> findByFilter(@Param("searchVo") Product searchVo); 

	Product findByProductCode(String productCode);
	
}
