// src/main/java/com/yedam/ac/repository/AcSalesRepository.java
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.sales2.domain.Sales;

public interface AcSalesRepository extends JpaRepository<Sales, String> {
    // 엔티티 필드명이 saleCode 인 점 주의!
    List<Sales> findTop50ByCompanyCodeAndSaleCodeContainingIgnoreCaseOrderBySalesDateDesc(
        String companyCode, String keyword);
}
