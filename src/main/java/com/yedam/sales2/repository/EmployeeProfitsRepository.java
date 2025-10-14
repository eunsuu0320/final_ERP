package com.yedam.sales2.repository;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.hr.domain.Employee;
import com.yedam.sales2.domain.Sales;

@Repository
public interface EmployeeProfitsRepository extends JpaRepository<Sales, String> {

	  // 사원별 매출 요약
	@Query(value = """
		    SELECT 
		        e.EMP_CODE AS empCode,
		        e.NAME AS name,
		        SUM(s.SALES_QTY) AS salesQty,
		        SUM(s.SALES_AMOUNT) AS salesAmount,
		        ROUND(SUM(s.SALES_AMOUNT) * 0.1) AS tax,
		        SUM(s.SALES_AMOUNT) + ROUND(SUM(s.SALES_AMOUNT) * 0.1) AS totalAmount
		    FROM SALES s
		    JOIN EMPLOYEE e ON s.EMP_CODE = e.EMP_CODE
		    WHERE s.COMPANY_CODE = :companyCode
		      AND (:year IS NULL OR TO_CHAR(s.SALES_DATE, 'YYYY') = TO_CHAR(:year))
		      AND (
		            :quarter IS NULL
		            OR (
		                (:quarter = 1 AND TO_NUMBER(TO_CHAR(s.SALES_DATE, 'MM')) BETWEEN 1 AND 3)
		                OR (:quarter = 2 AND TO_NUMBER(TO_CHAR(s.SALES_DATE, 'MM')) BETWEEN 4 AND 6)
		                OR (:quarter = 3 AND TO_NUMBER(TO_CHAR(s.SALES_DATE, 'MM')) BETWEEN 7 AND 9)
		                OR (:quarter = 4 AND TO_NUMBER(TO_CHAR(s.SALES_DATE, 'MM')) BETWEEN 10 AND 12)
		            )
		          )
		      AND (
		            :keyword IS NULL
		            OR LOWER(e.NAME) LIKE '%' || LOWER(:keyword) || '%'
		          )
		    GROUP BY e.EMP_CODE, e.NAME
		    ORDER BY e.NAME
		""", nativeQuery = true)
		List<Object[]> findEmployeeSalesSummaryRaw(
		    @Param("companyCode") String companyCode,
		    @Param("year") Integer year,
		    @Param("quarter") Integer quarter,
		    @Param("keyword") String keyword
		);


}