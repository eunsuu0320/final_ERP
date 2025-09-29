// src/main/java/com/yedam/ac/domain/BuyListView.java
package com.yedam.ac.domain;

import java.time.LocalDate;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "BUY_LIST_V")
@Immutable
@Getter
public class BuyListView {
    @Id
    @Column(name = "BUYCODE")
    private String buyCode;

    @Column(name = "PARTNERNAME")
    private String partnerName;

    @Column(name = "AMOUNTTOTAL")
    private Long amountTotal;

    @Column(name = "PRODUCTNAME")
    private String productName;

    @Column(name = "PURCHASEDATE")
    private LocalDate purchaseDate;

    @Column(name = "TAXCODE")
    private String taxCode;

    @Column(name = "COMPANYCODE")   // ★ 추가
    private String companyCode;
}
