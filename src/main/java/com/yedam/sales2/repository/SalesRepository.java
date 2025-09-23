package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.Sales;


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
	
}
