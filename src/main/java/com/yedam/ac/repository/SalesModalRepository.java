// src/main/java/com/yedam/ac/repository/SalesModalRepository.java
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.web.dto.SalesModalRow;
import com.yedam.sales2.domain.Sales;

public interface SalesModalRepository extends JpaRepository<Sales, String> {

    @Query(value = """
        SELECT *
        FROM (
            SELECT
              s.SALES_CODE    AS salesCode,     -- 🔁 컬럼명 교정
              s.CORRESPONDENT AS partnerName,
              s.PRODUCT_NAME  AS productName,
              s.SALES_AMOUNT  AS salesAmount,
              s.SALES_DATE    AS salesDate
            FROM SALES s
            WHERE s.COMPANY_CODE = :cc
              AND ( :kw IS NULL OR :kw = ''
                    OR UPPER(s.SALES_CODE)    LIKE '%'||UPPER(:kw)||'%'   -- 🔁 여기서도 교정
                    OR UPPER(s.CORRESPONDENT) LIKE '%'||UPPER(:kw)||'%'
                    OR UPPER(s.PRODUCT_NAME)  LIKE '%'||UPPER(:kw)||'%' )
            ORDER BY s.SALES_DATE DESC, s.SALES_CODE DESC                  -- 🔁 정렬 컬럼도 교정
        )
        WHERE ROWNUM <= 100
        """, nativeQuery = true)
    List<SalesModalRow> lookup(@Param("cc") String companyCode,
                               @Param("kw") String keyword);
}
