// src/main/java/com/yedam/ac/repository/BuyLookupRepository.java
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.Buy;
import com.yedam.ac.web.dto.BuyModalRow;

public interface BuyLookupRepository extends JpaRepository<Buy, String> {

    @Query(value = """
        SELECT *
        FROM (
            SELECT
              b.BUY_CODE      AS buyCode,
              b.PARTNER_NAME  AS partnerName,
              b.PRODUCT_NAME  AS productName,
              b.AMOUNT_TOTAL  AS amountTotal,
              b.PURCHASE_DATE AS purchaseDate
            FROM BUYS b
            WHERE b.COMPANY_CODE = :cc
              AND ( :kw IS NULL OR :kw = ''
                    OR UPPER(b.BUY_CODE)      LIKE '%'||UPPER(:kw)||'%'
                    OR UPPER(b.PARTNER_NAME)  LIKE '%'||UPPER(:kw)||'%'
                    OR UPPER(b.PRODUCT_NAME)  LIKE '%'||UPPER(:kw)||'%' )
            ORDER BY b.PURCHASE_DATE DESC, b.BUY_CODE DESC
        )
        WHERE ROWNUM <= 100
        """, nativeQuery = true)
    List<BuyModalRow> lookup(@Param("cc") String companyCode,
                             @Param("kw") String keyword);
}
