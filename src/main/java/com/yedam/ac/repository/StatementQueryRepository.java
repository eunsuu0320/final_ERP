package com.yedam.ac.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.ac.domain.SalesStatement; // 더미용(엔티티 아무거나 필요)
import com.yedam.ac.web.dto.UnifiedStatementRow;

@Repository
public interface StatementQueryRepository extends JpaRepository<SalesStatement, String> {

    @Query(value =
        "SELECT * FROM ( " +
        "  SELECT U1.*, ROWNUM rn FROM ( " +
        "    SELECT U0.* FROM ( " +
        /* ===== 매출 ===== */
        "      SELECT " +
        "         TO_CHAR(ss.VOUCHER_NO)   AS voucherNo, " +
        "         ss.VOUCHER_DATE          AS voucherDate, " +
        "         'SALES'                  AS type, " +
        "         ss.AMOUNT_TOTAL          AS amountTotal, " +
        "         ss.PARTNER_NAME          AS partnerName, " +
        "         ss.REMARK                AS remark " +
        "        FROM SALES_STATEMENT ss " +
        "       WHERE ss.COMPANY_CODE = :companyCode " +
        "      UNION ALL " +
        /* ===== 매입 ===== */
        "      SELECT " +
        "         TO_CHAR(bs.VOUCHER_NO)   AS voucherNo, " +
        "         bs.VOUCHER_DATE          AS voucherDate, " +
        "         'BUY'                    AS type, " +
        "         bs.AMOUNT_TOTAL          AS amountTotal, " +
        "         bs.PARTNER_NAME          AS partnerName, " +
        "         bs.REMARK                AS remark " +
        "        FROM BUY_STATEMENT bs " +
        "       WHERE bs.COMPANY_CODE = :companyCode " +
        "      UNION ALL " +
        /* ===== 수금 ===== */
        "      SELECT " +
        "         TO_CHAR(ms.VOUCHER_NO)   AS voucherNo, " +
        "         ms.VOUCHER_DATE          AS voucherDate, " +
        "         'MONEY'                  AS type, " +
        "         ms.AMOUNT_TOTAL          AS amountTotal, " +
        "         ms.PARTNER_NAME          AS partnerName, " +
        "         ms.REMARK                AS remark " +
        "        FROM MONEY_STATEMENT ms " +
        "       WHERE ms.COMPANY_CODE = :companyCode " +
        "      UNION ALL " +
        /* ===== 지급 ===== */
        "      SELECT " +
        "         TO_CHAR(ps.VOUCHER_NO)   AS voucherNo, " +
        "         ps.VOUCHER_DATE          AS voucherDate, " +
        "         'PAYMENT'                AS type, " +
        "         ps.AMOUNT_TOTAL          AS amountTotal, " +
        "         ps.PARTNER_NAME          AS partnerName, " +
        "         ps.REMARK                AS remark " +
        "        FROM PAYMENT_STATEMENT ps " +
        "       WHERE ps.COMPANY_CODE = :companyCode " +
        "    ) U0 " +
        "    WHERE (:type = 'ALL' OR U0.type = :type) " +
        "      AND ( :keyword = '' " +
        "            OR U0.partnerName LIKE '%' || :keyword || '%' " +
        "            OR U0.voucherNo   LIKE '%' || :keyword || '%' " +
        "          ) " +
        "      AND ( :voucherNo = '' OR U0.voucherNo LIKE '%' || :voucherNo || '%' ) " +
        "      AND ( :fromDate IS NULL OR U0.voucherDate >= :fromDate ) " +
        "      AND ( :toDate   IS NULL OR U0.voucherDate <= :toDate   ) " +
        "    ORDER BY U0.voucherDate DESC, U0.voucherNo DESC " +
        "  ) U1 WHERE ROWNUM <= :end " +
        ") X WHERE X.rn > :start",
        nativeQuery = true)
    List<UnifiedStatementRow> searchUnifiedList(
        String companyCode, String type, String keyword, String voucherNo,
        LocalDate fromDate, LocalDate toDate, int start, int end);

    @Query(value =
        "SELECT COUNT(1) FROM ( " +
        "  SELECT TO_CHAR(ss.VOUCHER_NO) AS voucherNo, ss.VOUCHER_DATE AS voucherDate, 'SALES' AS type, ss.PARTNER_NAME AS partnerName, ss.AMOUNT_TOTAL AS amountTotal, ss.REMARK AS remark " +
        "    FROM SALES_STATEMENT ss WHERE ss.COMPANY_CODE = :companyCode " +
        "  UNION ALL " +
        "  SELECT TO_CHAR(bs.VOUCHER_NO) AS voucherNo, bs.VOUCHER_DATE AS voucherDate, 'BUY' AS type, bs.PARTNER_NAME AS partnerName, bs.AMOUNT_TOTAL AS amountTotal, bs.REMARK AS remark " +
        "    FROM BUY_STATEMENT bs WHERE bs.COMPANY_CODE = :companyCode " +
        "  UNION ALL " +
        "  SELECT TO_CHAR(ms.VOUCHER_NO) AS voucherNo, ms.VOUCHER_DATE AS voucherDate, 'MONEY' AS type, ms.PARTNER_NAME AS partnerName, ms.AMOUNT_TOTAL AS amountTotal, ms.REMARK AS remark " +
        "    FROM MONEY_STATEMENT ms WHERE ms.COMPANY_CODE = :companyCode " +
        "  UNION ALL " +
        "  SELECT TO_CHAR(ps.VOUCHER_NO) AS voucherNo, ps.VOUCHER_DATE AS voucherDate, 'PAYMENT' AS type, ps.PARTNER_NAME AS partnerName, ps.AMOUNT_TOTAL AS amountTotal, ps.REMARK AS remark " +
        "    FROM PAYMENT_STATEMENT ps WHERE ps.COMPANY_CODE = :companyCode " +
        ") U0 " +
        "WHERE (:type = 'ALL' OR U0.type = :type) " +
        "  AND ( :keyword = '' " +
        "        OR U0.partnerName LIKE '%' || :keyword || '%' " +
        "        OR U0.voucherNo   LIKE '%' || :voucherNo || '%' " +
        "      ) " +
        "  AND ( :voucherNo = '' OR U0.voucherNo LIKE '%' || :voucherNo || '%' ) " +
        "  AND ( :fromDate IS NULL OR U0.voucherDate >= :fromDate ) " +
        "  AND ( :toDate   IS NULL OR U0.voucherDate <= :toDate   )",
        nativeQuery = true)
    long countUnified(
        String companyCode, String type, String keyword, String voucherNo,
        LocalDate fromDate, LocalDate toDate);
}
