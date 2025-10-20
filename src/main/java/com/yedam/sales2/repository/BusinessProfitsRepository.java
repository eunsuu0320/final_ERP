package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.repository.query.Param;
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
    
    
 // 차트 전용 — 분기 포함(QUARTER), 품목별 분기 이익률
    /* ✅ 차트 쿼리 (DB-agnostic)
    - 자바에서 계산해 넘긴 4개 (YEAR, QUARTER) 쌍만 OR로 매칭
    - 모든 DB에서 동작(TO_CHAR만 있으면 됨; 기존에도 쓰고 있었음)
 */
 @Query(value = """
     SELECT
         TO_CHAR(s.SALES_DATE, 'YYYY') AS YEAR,
         TO_CHAR(s.SALES_DATE, 'Q')    AS QUARTER,
         s.PRODUCT_NAME                AS PRODUCTNAME,
         ROUND(((SUM(s.SALES_AMOUNT) - SUM(s.COST_AMOUNT) - SUM(s.SALES_INCIDENTAL_COSTS))
              / NULLIF(SUM(s.SALES_AMOUNT), 0) * 100), 1) AS PROFITRATE
     FROM Sales s
     WHERE
       (
         (TO_CHAR(s.SALES_DATE, 'YYYY') = :y1 AND TO_CHAR(s.SALES_DATE, 'Q') = :q1) OR
         (TO_CHAR(s.SALES_DATE, 'YYYY') = :y2 AND TO_CHAR(s.SALES_DATE, 'Q') = :q2) OR
         (TO_CHAR(s.SALES_DATE, 'YYYY') = :y3 AND TO_CHAR(s.SALES_DATE, 'Q') = :q3) OR
         (TO_CHAR(s.SALES_DATE, 'YYYY') = :y4 AND TO_CHAR(s.SALES_DATE, 'Q') = :q4)
       )
       AND (:keyword IS NULL OR LOWER(s.PRODUCT_NAME) LIKE LOWER('%' || :keyword || '%'))
     GROUP BY TO_CHAR(s.SALES_DATE, 'YYYY'), TO_CHAR(s.SALES_DATE, 'Q'), s.PRODUCT_NAME
 """, nativeQuery = true)
 List<Map<String, Object>> findProfitRateForFourPairs(
     @Param("y1") String y1, @Param("q1") String q1,
     @Param("y2") String y2, @Param("q2") String q2,
     @Param("y3") String y3, @Param("q3") String q3,
     @Param("y4") String y4, @Param("q4") String q4,
     @Param("keyword") String keyword
 );
}
