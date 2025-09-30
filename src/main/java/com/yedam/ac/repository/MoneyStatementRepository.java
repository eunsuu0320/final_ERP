// src/main/java/com/yedam/ac/repository/MoneyStatementRepository.java
package com.yedam.ac.repository;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MoneyStatementRepository {
    private final JdbcTemplate jt;

    public int insert(
        String cc, String vno, LocalDate moneyDate, String moneyCode, String partnerName,
        String employee, String taxCode, BigDecimal supply, BigDecimal vat, BigDecimal total, String remark
    ){
        return jt.update("""
            INSERT INTO MONEY_STATEMENT (
              COMPANY_CODE, VOUCHER_NO, MONEY_DATE, MONEY_CODE,
              PARTNER_NAME, EMPLOYEE, TAX_CODE,
              AMOUNT_SUPPLY, AMOUNT_VAT, AMOUNT_TOTAL, REMARK
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """,
        cc, vno, Date.valueOf(moneyDate), moneyCode,
        partnerName, employee, taxCode,
        supply, vat, total, remark);
    }
}
