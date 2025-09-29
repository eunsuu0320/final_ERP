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
@Table(name = "BUYS")
public class Buy {

    @Id
    @Column(name = "BUY_CODE", length = 30, nullable = false)
    private String buyCode;

    @Column(name = "PARTNER_NAME", length = 200)
    private String partnerName;

    @Column(name = "PRODUCT_CODE", length = 50)
    private String productCode;

    @Column(name = "PRODUCT_NAME", length = 200)
    private String productName;

    @Column(name = "BUY_QTY")
    private Long buyQty;

    @Column(name = "BUY_UNIT_PRICE")
    private Long buyUnitPrice;

    @Column(name = "BUY_AMOUNT")
    private Long buyAmount;

    @Column(name = "TAX_CODE", length = 20)
    private String taxCode; // 기본 'TAXABLE'

    @Column(name = "AMOUNT_TOTAL")
    private Long amountTotal;

    @Column(name = "PURCHASE_DATE")
    private LocalDate purchaseDate;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
}
