package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.web.dto.SalesModalRow;
import com.yedam.sales2.domain.Sales;

public interface SalesModalRepository extends JpaRepository<Sales, String> {

    // 키워드가 없으면 최근 100건, 있으면 code/partner/product like 검색
    @Query(value =
        "SELECT * FROM (" +
        "  SELECT s.SALES_CODE AS salesCode," +
        "         s.CORRESPONDENT AS correspondent," +
        "         s.PRODUCT_NAME AS productName," +
        "         s.SALES_AMOUNT AS salesAmount," +
        "         s.SALES_DATE AS salesDate " +
        "    FROM SALES s " +
        "   WHERE (:kw IS NULL " +
        "          OR s.SALES_CODE LIKE '%'||:kw||'%' " +
        "          OR s.CORRESPONDENT LIKE '%'||:kw||'%' " +
        "          OR s.PRODUCT_NAME LIKE '%'||:kw||'%') " +
        "   ORDER BY s.SALES_DATE DESC, s.SALES_CODE DESC" +
        ") WHERE ROWNUM <= 100",
        nativeQuery = true)
    List<SalesModalRow> lookup(@Param("kw") String keyword);
}
