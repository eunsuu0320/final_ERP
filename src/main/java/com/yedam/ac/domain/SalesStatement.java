package com.yedam.ac.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "SALES_STATEMENT")
public class SalesStatement {

    // 전표번호가 PK 라고 가정합니다. (아니면 @Id 위치를 실제 PK로 바꾸세요)
    @Id
    @Column(name = "VOUCHER_NO", length = 20, nullable = false)
    private String voucherNo;             // 예: 2509-0001

    @Column(name = "VOUCHER_DATE")
    private LocalDate voucherDate;

    @Column(name = "SALES_CODE")
    private String salesCode;

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

    // (있다면) 숫자 백업 컬럼 유지용 – NOT NULL 풀었거나 트리거로 채우는 경우만 남겨두세요.
    // @Column(name = "VOUCHER_NO_NUM_BAK")
    // private Long voucherNoNumBak;
}
