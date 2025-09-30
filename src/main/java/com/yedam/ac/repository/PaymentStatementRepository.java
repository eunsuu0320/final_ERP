// src/main/java/com/yedam/ac/repository/PaymentStatementRepository.java
package com.yedam.ac.repository;

import java.sql.Date;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentStatementRepository {
    private final JdbcTemplate jt;

    public int insert(
        String cc, String vno, LocalDate payDate, String payCode, String partnerName,
        String employee, String taxCode, java.math.BigDecimal supply, java.math.BigDecimal vat,
        java.math.BigDecimal total, String remark
    ){
        return jt.update("""
            INSERT INTO PAYMENT_STATEMENT (
              COMPANY_CODE, VOUCHER_NO, PAYMENT_DATE, PAYMENT_CODE,
              PARTNER_NAME, EMPLOYEE, TAX_CODE,
              AMOUNT_SUPPLY, AMOUNT_VAT, AMOUNT_TOTAL, REMARK
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        cc, vno, Date.valueOf(payDate), payCode,
        partnerName, employee, taxCode,
        supply, vat, total, remark);
    }
}
