package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.ac.domain.Statement;
import com.yedam.ac.web.dto.UnifiedStatementRow;

@Repository
public interface StatementQueryRepository extends JpaRepository<Statement, Long> {

    // 목록: ROWNUM 상/하한으로 잘라서 반환 (Pageable 없음!)
    @Query(
      value =
        "SELECT * FROM ( " +
        "  SELECT U1.*, ROWNUM rn FROM ( " +
        "    SELECT U0.* FROM ( " +
        "      SELECT st.VOUCHER_NO AS voucherNo, ss.VOUCHER_DATE AS voucherDate, 'SALES' AS type, ss.AMOUNT_TOTAL AS amountTotal, ss.PARTNER_NAME AS partnerName, ss.REMARK AS remark " +
        "        FROM SALES_STATEMENT ss JOIN STATEMENT st ON st.VOUCHER_NO = ss.VOUCHER_NO " +
        "      UNION ALL " +
        "      SELECT bt.VOUCHER_NO, bs.VOUCHER_DATE, 'BUY', bs.AMOUNT_TOTAL, bs.PARTNER_NAME, bs.REMARK " +
        "        FROM BUY_STATEMENT bs JOIN STATEMENT bt ON bt.VOUCHER_NO = bs.VOUCHER_NO " +
        "      UNION ALL " +
        "      SELECT mt.VOUCHER_NO, ms.VOUCHER_DATE, 'MONEY', ms.AMOUNT_TOTAL, ms.PARTNER_NAME, ms.REMARK " +
        "        FROM MONEY_STATEMENT ms JOIN STATEMENT mt ON mt.VOUCHER_NO = ms.VOUCHER_NO " +
        "      UNION ALL " +
        "      SELECT pt.VOUCHER_NO, ps.VOUCHER_DATE, 'PAYMENT', ps.AMOUNT_TOTAL, ps.PARTNER_NAME, ps.REMARK " +
        "        FROM PAYMENT_STATEMENT ps JOIN STATEMENT pt ON pt.VOUCHER_NO = ps.VOUCHER_NO " +
        "    ) U0 " +
        "    WHERE (:type = 'ALL' OR U0.type = :type) " +
        "      AND (:keyword IS NULL OR U0.partnerName LIKE '%' || :keyword || '%' OR TO_CHAR(U0.voucherNo) LIKE '%' || :keyword || '%') " +
        "      AND (:voucherNo IS NULL OR TO_CHAR(U0.voucherNo) LIKE '%' || :voucherNo || '%') " +
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
        @Param("fromDate") java.time.LocalDate fromDate,
        @Param("toDate")   java.time.LocalDate toDate,
        @Param("start")    int start,
        @Param("end")      int end
    );

    // 전체 개수
    @Query(
      value =
        "SELECT COUNT(1) FROM ( " +
        "  SELECT st.VOUCHER_NO, ss.VOUCHER_DATE, 'SALES' AS type, ss.PARTNER_NAME, ss.AMOUNT_TOTAL, ss.REMARK " +
        "    FROM SALES_STATEMENT ss JOIN STATEMENT st ON st.VOUCHER_NO = ss.VOUCHER_NO " +
        "  UNION ALL " +
        "  SELECT bt.VOUCHER_NO, bs.VOUCHER_DATE, 'BUY', bs.PARTNER_NAME, bs.AMOUNT_TOTAL, bs.REMARK " +
        "    FROM BUY_STATEMENT bs JOIN STATEMENT bt ON bt.VOUCHER_NO = bs.VOUCHER_NO " +
        "  UNION ALL " +
        "  SELECT mt.VOUCHER_NO, ms.VOUCHER_DATE, 'MONEY', ms.PARTNER_NAME, ms.AMOUNT_TOTAL, ms.REMARK " +
        "    FROM MONEY_STATEMENT ms JOIN STATEMENT mt ON mt.VOUCHER_NO = ms.VOUCHER_NO " +
        "  UNION ALL " +
        "  SELECT pt.VOUCHER_NO, ps.VOUCHER_DATE, 'PAYMENT', ps.PARTNER_NAME, ps.AMOUNT_TOTAL, ps.REMARK " +
        "    FROM PAYMENT_STATEMENT ps JOIN STATEMENT pt ON pt.VOUCHER_NO = ps.VOUCHER_NO " +
        ") U0 " +
        "WHERE (:type = 'ALL' OR U0.type = :type) " +
        "  AND (:keyword IS NULL OR U0.PARTNER_NAME LIKE '%' || :keyword || '%' OR TO_CHAR(U0.VOUCHER_NO) LIKE '%' || :keyword || '%') " +
        "  AND (:voucherNo IS NULL OR TO_CHAR(U0.VOUCHER_NO) LIKE '%' || :voucherNo || '%') " +
        "  AND (:fromDate IS NULL OR U0.VOUCHER_DATE >= :fromDate) " +
        "  AND (:toDate   IS NULL OR U0.VOUCHER_DATE <= :toDate)",
      nativeQuery = true
    )
    long countUnified(
        @Param("type") String type,
        @Param("keyword") String keyword,
        @Param("voucherNo") String voucherNo,
        @Param("fromDate") java.time.LocalDate fromDate,
        @Param("toDate")   java.time.LocalDate toDate
    );
}
