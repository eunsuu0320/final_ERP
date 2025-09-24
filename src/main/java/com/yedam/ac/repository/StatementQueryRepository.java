package com.yedam.ac.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.ac.domain.Statement;
import com.yedam.ac.web.dto.UnifiedStatementRow;

@Repository
public interface StatementQueryRepository extends JpaRepository<Statement, String> {

    @Query(
      value =
        "SELECT * FROM ( " +
        "  SELECT U1.*, ROWNUM rn FROM ( " +
        "    SELECT U0.* FROM ( " +
        "      SELECT ss.VOUCHER_NO AS voucherNo, ss.VOUCHER_DATE AS voucherDate, 'SALES' AS type, " +
        "             ss.AMOUNT_TOTAL AS amountTotal, ss.PARTNER_NAME AS partnerName, ss.REMARK AS remark " +
        "        FROM SALES_STATEMENT ss " +
        "      UNION ALL " +
        "      SELECT bs.VOUCHER_NO AS voucherNo, bs.VOUCHER_DATE AS voucherDate, 'BUY'   AS type, " +
        "             bs.AMOUNT_TOTAL AS amountTotal, bs.PARTNER_NAME AS partnerName, bs.REMARK AS remark " +
        "        FROM BUY_STATEMENT bs " +
        "    ) U0 " +
        "    WHERE (:type = 'ALL' OR U0.type = :type) " +
        "      AND (:keyword IS NULL OR U0.partnerName LIKE '%' || :keyword || '%' OR U0.voucherNo LIKE '%' || :keyword || '%') " +
        "      AND (:voucherNo IS NULL OR U0.voucherNo LIKE '%' || :voucherNo || '%') " +
        "      AND (:fromDate IS NULL OR U0.voucherDate >= :fromDate) " +
        "      AND (:toDate   IS NULL OR U0.voucherDate <= :toDate) " +
        "    ORDER BY U0.voucherDate DESC, U0.voucherNo DESC " +
        "  ) U1 WHERE ROWNUM <= :end " +
        ") X WHERE X.rn > :start",
      nativeQuery = true
    )
    List<UnifiedStatementRow> searchUnifiedList(
        @Param("type") String type,
        @Param("keyword") String keyword,
        @Param("voucherNo") String voucherNo,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate")   LocalDate toDate,
        @Param("start")    int start,
        @Param("end")      int end
    );

    @Query(
      value =
        "SELECT COUNT(1) FROM ( " +
        "  SELECT ss.VOUCHER_NO AS voucherNo, ss.VOUCHER_DATE AS voucherDate, 'SALES' AS type, ss.PARTNER_NAME AS partnerName, ss.AMOUNT_TOTAL AS amountTotal, ss.REMARK AS remark " +
        "    FROM SALES_STATEMENT ss " +
        "  UNION ALL " +
        "  SELECT bs.VOUCHER_NO AS voucherNo, bs.VOUCHER_DATE AS voucherDate, 'BUY'   AS type, bs.PARTNER_NAME AS partnerName, bs.AMOUNT_TOTAL AS amountTotal, bs.REMARK AS remark " +
        "    FROM BUY_STATEMENT bs " +
        ") U0 " +
        "WHERE (:type = 'ALL' OR U0.type = :type) " +
        "  AND (:keyword IS NULL OR U0.partnerName LIKE '%' || :keyword || '%' OR U0.voucherNo LIKE '%' || :keyword || '%') " +
        "  AND (:voucherNo IS NULL OR U0.voucherNo LIKE '%' || :voucherNo || '%') " +
        "  AND (:fromDate IS NULL OR U0.voucherDate >= :fromDate) " +
        "  AND (:toDate   IS NULL OR U0.voucherDate <= :toDate)",
      nativeQuery = true
    )
    long countUnified(
        @Param("type") String type,
        @Param("keyword") String keyword,
        @Param("voucherNo") String voucherNo,
        @Param("fromDate") LocalDate fromDate,
        @Param("toDate")   LocalDate toDate
    );
}
