package com.yedam.ac.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity @Table(name="BUY_STATEMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class BuyStatement {

    @Id
    @Column(name = "VOUCHER_NO")
    private String voucherNo;          // 부모 STATEMENT 번호 그대로 사용 (setter 없음)

    @Column(name = "VOUCHER_DATE")
    private LocalDate voucherDate;

    @Column(name = "BUY_CODE")
    private String buyCode;

    @Column(name = "PARTNER_NAME")
    private String partnerName;

    @Column(name = "EMPLOYEE")
    private String employee;

    @Column(name = "TAX_CODE")
    private String taxCode;

    @Column(name = "AMOUNT_SUPPLY")
    private Long amountSupply;

    @Column(name = "AMOUNT_VAT")
    private Long amountVat;

    @Column(name = "AMOUNT_TOTAL")
    private Long amountTotal;

    @Column(name = "REMARK")
    private String remark;
}

