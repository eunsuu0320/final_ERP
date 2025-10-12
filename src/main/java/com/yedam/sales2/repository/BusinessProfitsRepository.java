package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.Sales;

@Repository
public interface BusinessProfitsRepository extends JpaRepository<Sales, String> {

    // ✅ 품목별 집계 쿼리 (JPQL)
    @Query("""
        SELECT new map(
            s.productCode as productCode,
            s.productName as productName,
            SUM(s.salesQty) as qty,
            ROUND(AVG(s.salesPrice)) as salePrice,
            SUM(s.salesAmount) as saleAmt,
            ROUND(AVG(s.costUnitPrice)) as costPrice,
            SUM(s.costAmount) as costAmt,
            SUM(s.salesIncidentalCosts) as expAmt,
            ROUND((SUM(s.salesAmount) - SUM(s.costAmount) - SUM(s.salesIncidentalCosts)) / SUM(s.salesAmount) * 100, 1) as profitRate
        )
        FROM Sales s
        WHERE (:year IS NULL OR TO_CHAR(s.salesDate, 'YYYY') = :year)
          AND (:quarter IS NULL OR TO_CHAR(s.salesDate, 'Q') = :quarter)
          AND (:keyword IS NULL OR LOWER(s.productName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        GROUP BY s.productCode, s.productName
        ORDER BY s.productCode
    """)
    List<Map<String, Object>> findSalesProfitList(
            @Param("year") String year,
            @Param("quarter") String quarter,
            @Param("keyword") String keyword);
}
