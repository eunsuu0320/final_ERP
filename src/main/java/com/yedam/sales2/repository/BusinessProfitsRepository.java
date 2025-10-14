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

    // 품목별 집계 쿼리 (JPQL)
    @Query(value = """
		       SELECT 
		    s.PRODUCT_CODE        AS PRODUCTCODE,
		    s.PRODUCT_NAME        AS PRODUCTNAME,
		    SUM(s.SALES_QTY)      AS QTY,
		    ROUND(AVG(s.SALES_PRICE)) AS SALEPRICE,
		    SUM(s.SALES_AMOUNT)   AS SALEAMT,
		    ROUND(AVG(s.COST_UNIT_PRICE)) AS COSTPRICE,
		    SUM(s.COST_AMOUNT)    AS COSTAMT,
		    SUM(s.SALES_INCIDENTAL_COSTS) AS EXPAMT,
		    ROUND(
		        ((SUM(s.SALES_AMOUNT) - SUM(s.COST_AMOUNT) - SUM(s.SALES_INCIDENTAL_COSTS))
		        / NULLIF(SUM(s.SALES_AMOUNT), 0) * 100), 1
		    ) AS PROFITRATE
		FROM Sales s
		WHERE (:year IS NULL OR TO_CHAR(s.SALES_DATE, 'YYYY') = :year)
		  AND (:quarter IS NULL OR TO_CHAR(s.SALES_DATE, 'Q') = :quarter)
		  AND (:keyword IS NULL OR LOWER(s.PRODUCT_NAME) LIKE LOWER('%' || :keyword || '%'))
		GROUP BY s.PRODUCT_CODE, s.PRODUCT_NAME
		ORDER BY s.PRODUCT_CODE
    """,  nativeQuery = true)
    
    List<Map<String, Object>> findSalesProfitList(
            @Param("year") String year,
            @Param("quarter") String quarter,
            @Param("keyword") String keyword);
}
