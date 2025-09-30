package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.EmpPlan;
import com.yedam.sales2.domain.Sales;

@Repository
public interface SalesRepository extends JpaRepository<Sales, String> {

    // 작년 영업 매출 (모달창 조회용)
    @Query(value = "SELECT "
            + "    TO_CHAR(SALES_DATE, 'Q') AS SALES_QUARTER, "
            + "    SUM(SALES_AMOUNT) AS TOTAL_SALES_AMOUNT, "
            + "    SUM(COST_AMOUNT) AS TOTAL_COST_AMOUNT, "
            + "    SUM(PROFIT_AMOUNT) AS TOTAL_PROFIT_AMOUNT "
            + "FROM "
            + "    SALES "
            + "WHERE "
            + "    EXTRACT(YEAR FROM SALES_DATE) = EXTRACT(YEAR FROM SYSDATE) - 1 "
            + "GROUP BY "
            + "    TO_CHAR(SALES_DATE, 'Q') "
            + "ORDER BY "
            + "    SALES_QUARTER", nativeQuery = true)
    List<Map<String, Object>> findLastYearSalesData();
    
 // 사원별 영업매출 목록
    @Query(value =
    	    "SELECT  "
    	    + "    s.EMP_CODE                          AS emp_code, "
    	    + "    e.NAME                              AS EMPNAME, "
    	    + "    COUNT(DISTINCT s.CORRESPONDENT)     AS CUSTOMERCOUNT, "
    	    + "    SUM(s.SALES_AMOUNT)                 AS LASTYEARSALES, "
    	    + "    ROUND(AVG(s.COST_UNIT_PRICE), 2)    AS LASTYEARCOST, "
    	    + "    SUM(s.PROFIT_AMOUNT)                AS LASTYEARPROFIT "
    	    + "FROM SALES s "
    	    + "JOIN EMPLOYEE e  "
    	    + "    ON s.EMP_CODE = e.EMP_CODE "
    	    + "WHERE EXTRACT(YEAR FROM s.SALES_DATE) = EXTRACT(YEAR FROM SYSDATE) - 1 "
    	    + "GROUP BY s.EMP_CODE, e.NAME "
    	    + "ORDER BY s.EMP_CODE",
    	    nativeQuery = true)
    List<Map<String, Object>> findEmpPlanLastYear();

}