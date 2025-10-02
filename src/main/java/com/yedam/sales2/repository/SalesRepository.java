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
    	    "SELECT          d.esp_code                         AS ESPCODE  ,\r\n"
    	    + "    	         e.EMP_CODE                          AS emp_code, \r\n"
    	    + "    	         e.NAME                              AS EMPNAME, \r\n"
    	    + "    	         COUNT(DISTINCT s.CORRESPONDENT)     AS CUSTOMERCOUNT, \r\n"
    	    + "    	         SUM(s.SALES_AMOUNT)                 AS LASTYEARSALES, \r\n"
    	    + "    	         ROUND(AVG(s.COST_UNIT_PRICE), 2)    AS LASTYEARCOST, \r\n"
    	    + "    	         SUM(s.PROFIT_AMOUNT)                AS LASTYEARPROFIT \r\n"
    	    + "    	     FROM EMPLOYEE e\r\n"
    	    + "              join   (   select \r\n"
    	    + "                                   s.sales_plan_code,\r\n"
    	    + "                                   s.company_code,\r\n"
    	    + "                                   ep.emp_code,\r\n"
    	    + "                                   ep.esp_code\r\n"
    	    + "                        from sales_plan s\r\n"
    	    + "                        join esp_plan ep on s.sales_plan_code = ep.sp_code\r\n"
    	    + "                        where to_char( s.plan_year, 'yyyy')= ?2) d\r\n"
    	    + "                 on (e.emp_code = d.emp_code and e.company_code = d.company_code)\r\n"
    	    + "    	     LEFT JOIN SALES s \r\n"
    	    + "    	         ON  (s.EMP_CODE = e.EMP_CODE AND EXTRACT(YEAR FROM s.SALES_DATE) = EXTRACT(YEAR FROM SYSDATE) - 1)\r\n"
    	    + "             where e.company_code =?1\r\n"
    	    + "    	     GROUP BY d.esp_code, e.EMP_CODE, e.NAME \r\n"
    	    + "    	     ORDER BY e.EMP_CODE",
    	    nativeQuery = true)
    List<Map<String, Object>> findEmpPlanLastYear(String companyCode, String planYear);

}