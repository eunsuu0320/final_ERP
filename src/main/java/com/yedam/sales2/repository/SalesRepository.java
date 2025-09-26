package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.Sales;
import com.yedam.sales2.domain.SalesPlan;


// 영업계획 목록의 쿼리문
@Repository
public interface SalesRepository extends JpaRepository<Sales, String>{

	@Query(value = "SELECT TO_CHAR(SALES_DATE, 'YYYY') AS \"salesYear\", "
            + "COUNT(DISTINCT CORRESPONDENT) AS \"correspondentCount\", "
            + "SUM(SALES_AMOUNT) AS \"totalSalesAmount\", "
            + "SUM(PROFIT_AMOUNT) AS \"totalProfitAmount\" "
            + "FROM SALES "
            + "GROUP BY TO_CHAR(SALES_DATE, 'YYYY') "
            + "ORDER BY \"salesYear\"", nativeQuery = true)
	
 List<Map<String, Object>> findSalesStatsByYear();
	
// 전년 영업계획목록
	@Query(value = "SELECT"
	        + "    q.SALES_QUARTER,"
	        + "    COALESCE(SUM(s.SALES_AMOUNT), 0) AS TOTAL_SALES_AMOUNT,"
	        + "    COALESCE(SUM(s.COST_AMOUNT), 0) AS TOTAL_COST_AMOUNT,"
	        + "    COALESCE(SUM(s.SALES_AMOUNT) - SUM(s.COST_AMOUNT), 0) AS TOTAL_PROFIT_AMOUNT"
	        + " FROM ("
	        + "    SELECT LEVEL AS SALES_QUARTER"
	        + "    FROM dual"
	        + "    CONNECT BY LEVEL <= 4"
	        + ") q"
	        + " LEFT JOIN SALES s"
	        + "    ON TO_CHAR(s.SALES_DATE, 'Q') = TO_CHAR(q.SALES_QUARTER)"
	        + "   AND EXTRACT(YEAR FROM s.SALES_DATE) = EXTRACT(YEAR FROM SYSDATE) - 1"
	        + " GROUP BY q.SALES_QUARTER"
	        + " ORDER BY q.SALES_QUARTER", nativeQuery = true)
	List<Map<String, Object>> findSalesPlanData();
	
}
