// src/main/java/com/yedam/ac/domain/MoneyStatement.java
package com.yedam.ac.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MONEY_STATEMENT")
@Getter @Setter
public class MoneyStatement {

    @Id
    @Column(name = "VOUCHER_NO", length = 20)
    private String voucherNo;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;

    @Column(name = "MONEY_DATE", nullable = false)
    private LocalDate moneyDate;

    @Column(name = "MONEY_CODE", length = 50)
    private String moneyCode;

    @Column(name = "PARTNER_NAME", length = 100)
    private String partnerName;

    @Column(name = "EMPLOYEE", length = 50)
    private String employee;

    @Column(name = "TAX_CODE", length = 20)
    private String taxCode;

    @Column(name = "AMOUNT_SUPPLY")
    private BigDecimal amountSupply;

    @Column(name = "AMOUNT_VAT")
    private BigDecimal amountVat;

    @Column(name = "AMOUNT_TOTAL")
    private BigDecimal amountTotal;

    @Column(name = "REMARK", length = 500)
    private String remark;
}
