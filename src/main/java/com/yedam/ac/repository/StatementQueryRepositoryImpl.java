// src/main/java/com/yedam/ac/repository/StatementQueryRepositoryImpl.java
package com.yedam.ac.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class StatementQueryRepositoryImpl implements StatementQueryRepositoryCustom {

    private final JdbcTemplate jt;

    @Override
    public List<UnifiedStatementRow> searchUnifiedList(
            String cc, String type, String keyword, String voucherNo,
            LocalDate fromDate, LocalDate toDate, int start, int end) {

        String sql =
            "SELECT * FROM ( " +
            "  SELECT U1.*, ROWNUM rn FROM ( " +
            "    SELECT U0.* FROM ( " +
            "      SELECT TO_CHAR(ss.VOUCHER_NO) AS voucherNo, ss.VOUCHER_DATE AS voucherDate, 'SALES'   AS type, " +
            "             ss.AMOUNT_TOTAL AS amountTotal, ss.PARTNER_NAME AS partnerName, ss.REMARK AS remark " +
            "        FROM SALES_STATEMENT ss WHERE ss.COMPANY_CODE = ? " +
            "      UNION ALL " +
            "      SELECT TO_CHAR(bs.VOUCHER_NO) AS voucherNo, bs.VOUCHER_DATE AS voucherDate, 'BUY'     AS type, " +
            "             bs.AMOUNT_TOTAL AS amountTotal, bs.PARTNER_NAME AS partnerName, bs.REMARK AS remark " +
            "        FROM BUY_STATEMENT bs WHERE bs.COMPANY_CODE = ? " +
            "      UNION ALL " +
            "      SELECT TO_CHAR(ms.VOUCHER_NO) AS voucherNo, ms.VOUCHER_DATE AS voucherDate, 'MONEY'   AS type, " +
            "             ms.AMOUNT_TOTAL AS amountTotal, ms.PARTNER_NAME AS partnerName, ms.REMARK AS remark " +
            "        FROM MONEY_STATEMENT ms WHERE ms.COMPANY_CODE = ? " +
            "      UNION ALL " +
            "      SELECT TO_CHAR(ps.VOUCHER_NO) AS voucherNo, ps.VOUCHER_DATE AS voucherDate, 'PAYMENT' AS type, " +
            "             ps.AMOUNT_TOTAL AS amountTotal, ps.PARTNER_NAME AS partnerName, ps.REMARK AS remark " +
            "        FROM PAYMENT_STATEMENT ps WHERE ps.COMPANY_CODE = ? " +
            "    ) U0 " +
            "    WHERE (? = 'ALL' OR U0.type = ?) " +
            "      AND (? = '' OR U0.partnerName LIKE '%' || ? || '%' OR U0.voucherNo LIKE '%' || ? || '%') " +
            "      AND (? = '' OR U0.voucherNo   LIKE '%' || ? || '%') " +
            "      AND (? IS NULL OR U0.voucherDate >= ?) " +
            "      AND (? IS NULL OR U0.voucherDate <= ?) " +
            "    ORDER BY U0.voucherDate DESC, U0.voucherNo DESC " +
            "  ) U1 WHERE ROWNUM <= ? " +
            ") X WHERE X.rn > ?";

        return jt.query(sql, (rs, i) -> new UnifiedStatementRow(
                    rs.getString("voucherNo"),
                    rs.getDate("voucherDate").toLocalDate(),
                    rs.getString("type"),
                    rs.getLong("amountTotal"),
                    rs.getString("partnerName"),
                    rs.getString("remark"),
                    null // productName
                ),
                cc, cc, cc, cc,
                type, type,
                keyword, keyword, keyword,
                voucherNo, voucherNo,
                toSqlDate(fromDate), toSqlDate(fromDate),
                toSqlDate(toDate),   toSqlDate(toDate),
                end, start
        );
    }

    @Override
    public long countUnified(
            String cc, String type, String keyword, String voucherNo,
            LocalDate fromDate, LocalDate toDate) {

        String sql =
            "SELECT COUNT(1) FROM ( " +
            "  SELECT TO_CHAR(ss.VOUCHER_NO) AS voucherNo, ss.VOUCHER_DATE AS voucherDate, 'SALES'   AS type, ss.PARTNER_NAME AS partnerName, ss.AMOUNT_TOTAL AS amountTotal, ss.REMARK AS remark " +
            "    FROM SALES_STATEMENT ss   WHERE ss.COMPANY_CODE = ? " +
            "  UNION ALL " +
            "  SELECT TO_CHAR(bs.VOUCHER_NO) AS voucherNo, bs.VOUCHER_DATE AS voucherDate, 'BUY'     AS type, bs.PARTNER_NAME AS partnerName, bs.AMOUNT_TOTAL AS amountTotal, bs.REMARK AS remark " +
            "    FROM BUY_STATEMENT bs      WHERE bs.COMPANY_CODE = ? " +
            "  UNION ALL " +
            "  SELECT TO_CHAR(ms.VOUCHER_NO) AS voucherNo, ms.VOUCHER_DATE AS voucherDate, 'MONEY'   AS type, ms.PARTNER_NAME AS partnerName, ms.AMOUNT_TOTAL AS amountTotal, ms.REMARK AS remark " +
            "    FROM MONEY_STATEMENT ms     WHERE ms.COMPANY_CODE = ? " +
            "  UNION ALL " +
            "  SELECT TO_CHAR(ps.VOUCHER_NO) AS voucherNo, ps.VOUCHER_DATE AS voucherDate, 'PAYMENT' AS type, ps.PARTNER_NAME AS partnerName, ps.AMOUNT_TOTAL AS amountTotal, ps.REMARK AS remark " +
            "    FROM PAYMENT_STATEMENT ps   WHERE ps.COMPANY_CODE = ? " +
            ") U0 " +
            "WHERE (? = 'ALL' OR U0.type = ?) " +
            "  AND (? = '' OR U0.partnerName LIKE '%' || ? || '%' OR U0.voucherNo LIKE '%' || ? || '%') " +
            "  AND (? = '' OR U0.voucherNo   LIKE '%' || ? || '%') " +
            "  AND (? IS NULL OR U0.voucherDate >= ?) " +
            "  AND (? IS NULL OR U0.voucherDate <= ?)";

        return jt.queryForObject(sql, Long.class,
                cc, cc, cc, cc,
                type, type,
                keyword, keyword, keyword,
                voucherNo, voucherNo,
                toSqlDate(fromDate), toSqlDate(fromDate),
                toSqlDate(toDate),   toSqlDate(toDate)
        );
    }

    private static Date toSqlDate(LocalDate d) {
        return (d == null) ? null : Date.valueOf(d);
    }
}
