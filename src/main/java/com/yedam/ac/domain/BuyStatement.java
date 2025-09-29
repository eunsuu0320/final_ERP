// src/main/java/com/yedam/ac/domain/BuyStatement.java
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
@Table(name = "BUY_STATEMENT")
public class BuyStatement extends BaseCompanyEntity {

    @Id
    @Column(name = "VOUCHER_NO", length = 20, nullable = false)
    private String voucherNo;

    @Column(name = "VOUCHER_DATE")  private LocalDate voucherDate;
    @Column(name = "BUY_CODE")      private String buyCode;
    @Column(name = "BUY_DATE")      private LocalDate buyDate;
    @Column(name = "PARTNER_CODE")  private String partnerCode;
    @Column(name = "PARTNER_NAME")  private String partnerName;
    @Column(name = "EMPLOYEE")      private String employee;
    @Column(name = "PRODUCT_CODE")  private String productCode;
    @Column(name = "UNIT_PRICE")    private Long unitPrice;
    @Column(name = "TAX_CODE")      private String taxCode;
    @Column(name = "AMOUNT_SUPPLY") private Long amountSupply;
    @Column(name = "AMOUNT_VAT")    private Long amountVat;
    @Column(name = "AMOUNT_TOTAL")  private Long amountTotal;
    @Column(name = "REMARK")        private String remark;
}
