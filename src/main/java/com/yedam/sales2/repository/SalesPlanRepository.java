package com.yedam.sales2.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales2.domain.SalesPlan;

@Repository
public interface SalesPlanRepository extends JpaRepository<SalesPlan, Integer> { 

	@Query("SELECT CASE WHEN COUNT(sp) > 0 THEN true ELSE false END " +
	           "FROM SalesPlan sp WHERE EXTRACT(YEAR FROM sp.planYear) = :year")
	    boolean existsByPlanYear(@Param("year") int year);
	    
    // 특정 기간에 해당하는 SalesPlan이 존재하는지 확인하는 메서드
    boolean existsByPlanYearBetween(Date startDate, Date endDate);
	
	// 영업계획목록
	@Query(value = "WITH YEARLY_PLAN_DATA AS ( "
	        + "    /* 1단계: 년도별 목표 데이터를 집계 */ "
	        + "    SELECT "
	        + "        EXTRACT(YEAR FROM sp.PLAN_YEAR) AS salesYear, "
	        + "        SUM(spd.PURP_SALES) AS totalSalesAmount, "
	        + "        SUM(spd.PURP_PROFIT_AMT) AS totalProfitAmount, "
	        + "        SUM(spd.NEW_VEND_CNT) AS correspondentCount "
	        + "    FROM SALES_PLAN sp "
	        + "    JOIN SALES_PLAN_DETAIL spd "
	        + "    ON sp.SALES_PLAN_CODE = spd.SALES_PLAN_CODE "
	        + "    GROUP BY EXTRACT(YEAR FROM sp.PLAN_YEAR) "
	        + ") "
	        + "SELECT "
	        + "    salesYear AS salesYear, "
	        + "    totalSalesAmount AS totalSalesAmount, "
	        + "    totalProfitAmount AS totalProfitAmount, "
	        + "    correspondentCount AS correspondentCount, "
	        + "    ROUND( "
	        + "        ( (correspondentCount - LAG(correspondentCount, 1, 0) OVER (ORDER BY salesYear)) "
	        + "          / NULLIF(LAG(correspondentCount, 1, 0) OVER (ORDER BY salesYear), 0) "
	        + "        ) * 100, 2 "
	        + "    ) AS retradeRate "
	        + "FROM YEARLY_PLAN_DATA "
	        + "ORDER BY salesYear",
	       nativeQuery = true)
	List<Map<String, Object>> findSalesStatsByYear();
	
	@Query(value = "SELECT * FROM sales_plan WHERE EXTRACT(YEAR FROM plan_year) = :year", nativeQuery = true)
	List<SalesPlan> findByPlanYear(@Param("year") Integer year);


}