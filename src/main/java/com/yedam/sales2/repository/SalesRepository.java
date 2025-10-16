package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    public interface TopOutstandingProjection {
        String getPartnerCode();
        String getPartnerName();
        java.math.BigDecimal getTotalUnrctBaln();
    }
    @Query(value =
    	    "SELECT          d.esp_code                         AS ESPCODE  , "
    	    + "    	         e.EMP_CODE                          AS emp_code,  "
    	    + "    	         e.NAME                              AS EMPNAME,  "
    	    + "    	         COUNT(DISTINCT s.CORRESPONDENT)     AS CUSTOMERCOUNT,  "
    	    + "    	         SUM(s.SALES_AMOUNT)                 AS LASTYEARSALES,  "
    	    + "    	         ROUND(AVG(s.COST_UNIT_PRICE), 2)    AS LASTYEARCOST,  "
    	    + "    	         SUM(s.PROFIT_AMOUNT)                AS LASTYEARPROFIT  "
    	    + "    	     FROM EMPLOYEE e "
    	    + "              join   (   select  "
    	    + "                                   s.sales_plan_code, "
    	    + "                                   s.company_code, "
    	    + "                                   ep.emp_code, "
    	    + "                                   ep.esp_code "
    	    + "                        from sales_plan s "
    	    + "                        join esp_plan ep on s.sales_plan_code = ep.sp_code "
    	    + "                        where to_char( s.plan_year, 'yyyy')= ?2) d "
    	    + "                 on (e.emp_code = d.emp_code and e.company_code = d.company_code) "
    	    + "    	     LEFT JOIN SALES s  "
    	    + "    	         ON  (s.EMP_CODE = e.EMP_CODE AND EXTRACT(YEAR FROM s.SALES_DATE) = EXTRACT(YEAR FROM SYSDATE) - 1) "
    	    + "             where e.company_code =?1 "
    	    + "    	     GROUP BY d.esp_code, e.EMP_CODE, e.NAME  "
    	    + "    	     ORDER BY e.EMP_CODE",
    	    nativeQuery = true)
    List<Map<String, Object>> findEmpPlanLastYear(String companyCode, String planYear);

    
    // 미수금 top5
    // 미수잔액 TOP N (오라클 호환, 바인드 OK)
    @Query(value =
            "SELECT * FROM ( " +
            "  SELECT " +
            "    i.partner_code            AS partnerCode, " +
            "    i.partner_name            AS partnerName, " +
            "    SUM(NVL(i.unrct_baln,0))  AS totalUnrctBaln " +
            "  FROM invoice i " +
            "  WHERE TRIM(i.company_code) = :companyCode " +
            "    AND NVL(UPPER(TRIM(i.is_current_version)),'N') = 'Y' " +
            "  GROUP BY i.partner_code, i.partner_name " +
            "  ORDER BY SUM(NVL(i.unrct_baln,0)) DESC " +
            ") t " +
            "WHERE ROWNUM <= :limit",
            nativeQuery = true)
        List<Map<String,Object>> findTopOutstanding(
            @Param("companyCode") String companyCode,
            @Param("limit") int limit
        );
    }
