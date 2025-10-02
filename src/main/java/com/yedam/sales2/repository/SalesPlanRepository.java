package com.yedam.sales2.repository;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.query.Procedure;
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
	@Query(value = "WITH YEARLY_PLAN_DATA AS (\r\n"
			+ "    /* 1단계: 계획 코드별/년도별 목표 데이터를 집계 */\r\n"
			+ "    SELECT\r\n"
			+ "        sp.SALES_PLAN_CODE, -- 📌 계획 코드 추가\r\n"
			+ "        EXTRACT(YEAR FROM sp.PLAN_YEAR) AS SALESYEAR,\r\n"
			+ "        SUM(spd.PURP_SALES) AS TOTALSALESAMOUNT,\r\n"
			+ "        SUM(spd.PURP_PROFIT_AMT) AS TOTALPROFITAMOUNT,\r\n"
			+ "        SUM(spd.NEW_VEND_CNT) AS CORRESPONDENTCOUNT\r\n"
			+ "    FROM SALES_PLAN sp\r\n"
			+ "    JOIN SALES_PLAN_DETAIL spd\r\n"
			+ "        ON sp.SALES_PLAN_CODE = spd.SALES_PLAN_CODE\r\n"
			+ "    GROUP BY \r\n"
			+ "        sp.SALES_PLAN_CODE, -- 📌 그룹화 기준에 추가\r\n"
			+ "        EXTRACT(YEAR FROM sp.PLAN_YEAR)\r\n"
			+ ")\r\n"
			+ "SELECT\r\n"
			+ "    YPD.SALES_PLAN_CODE AS salesPlanCode, -- 📌 최종 결과에 포함 (JS 변수명과 일치하도록 별칭 부여)\r\n"
			+ "    YPD.SALESYEAR,\r\n"
			+ "    YPD.TOTALSALESAMOUNT,\r\n"
			+ "    YPD.TOTALPROFITAMOUNT,\r\n"
			+ "    YPD.CORRESPONDENTCOUNT,\r\n"
			+ "    -- 재거래율 계산 (이젠 계획 코드별로 순서가 정해짐)\r\n"
			+ "    ROUND(\r\n"
			+ "        ( (YPD.CORRESPONDENTCOUNT - LAG(YPD.CORRESPONDENTCOUNT, 1, 0) OVER (ORDER BY YPD.SALESYEAR, YPD.SALES_PLAN_CODE))\r\n"
			+ "          / NULLIF(LAG(YPD.CORRESPONDENTCOUNT, 1, 0) OVER (ORDER BY YPD.SALESYEAR, YPD.SALES_PLAN_CODE), 0)\r\n"
			+ "        ) * 100, 2\r\n"
			+ "    ) AS RETRADE_RATE\r\n"
			+ "FROM YEARLY_PLAN_DATA YPD\r\n"
			+ "ORDER BY YPD.SALESYEAR, YPD.SALES_PLAN_CODE",
	       nativeQuery = true)
	List<Map<String, Object>> findSalesStatsByYear();
	
	 // 네이티브 쿼리: planYear의 연도가 일치하는 데이터 조회
	@Query(value = "SELECT * FROM sales_plan WHERE EXTRACT(YEAR FROM plan_year) = :year", nativeQuery = true)
	List<SalesPlan> findByPlanYear(@Param("year") Integer year);
	
	
	// 영업등록 후 사원배분
	@Procedure(name = "PR_EMP_PLAN")
	void PR_EMP_PLAN(
	        @Param("P_COMPANY_CODE") String companyCode,
	        @Param("P_PLAN_YEAR") String planYear
			);
}