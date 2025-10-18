package com.yedam.sales2.repository;

import java.util.Date;
import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

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
	List<Object[]> findEmployeeSalesSummaryRaw(@Param("companyCode") String companyCode, @Param("year") Integer year,
			@Param("quarter") Integer quarter, @Param("keyword") String keyword);


	// 사원관리 모달
	 interface EmpPartnerView {
	        String getPartnerCode();
	        String getPartnerName();
	        java.math.BigDecimal getSalesAmount();  // 필요시 BigDecimal로 변경
	        java.math.BigDecimal   getCollectAmt();   // 필요시 BigDecimal로 변경
	    }

	    // 사원관리 모달: 업체 기준 (주문=기간합, 수금=누적합)
	    @Query("""
	            SELECT
	              p.partnerCode  AS partnerCode,
	              p.partnerName  AS partnerName,

	              COALESCE((
	                SELECT SUM(o.totalAmount)
	                FROM Orders o
	                WHERE o.partner = p
	                  AND o.isCurrentVersion = 'Y'
	                  AND o.createDate >= :fromDate
	                  AND o.createDate <  :toDate
	              ), 0) AS salesAmount,

	              COALESCE((
	                SELECT SUM(c.recpt)
	                FROM Collection c
	                WHERE c.partner = p
	              ), 0) AS collectAmt

	            FROM Partner p
	            WHERE p.companyCode = :companyCode
	              AND p.managerEmp.empCode = :empCode
	              AND (:keyword IS NULL OR LOWER(p.partnerName) LIKE LOWER(CONCAT('%', :keyword, '%')))
	            ORDER BY p.partnerName
	        """)
	    List<EmpPartnerView> findEmpPartnersSums_ByPartnerBase(
	            @Param("companyCode") String companyCode,
	            @Param("empCode") String empCode,
	            @Param("fromDate") Date fromDate,
	            @Param("toDate") Date toDate,
	            @Param("keyword") String keyword
	    );
	}