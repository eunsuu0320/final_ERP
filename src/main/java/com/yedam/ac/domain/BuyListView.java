package com.yedam.ac.domain;

import java.time.LocalDate;

import org.hibernate.annotations.Immutable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Table(name = "BUY_LIST_V")  // 오라클 VIEW
@Immutable                  // 읽기 전용
@Getter
public class BuyListView {

    @Id
    @Column(name = "BUYCODE")     // 뷰 별칭 buyCode → 오라클 컬럼/별칭 대소문자 무관
    private String buyCode;

    @Column(name = "PARTNERNAME")
    private String partnerName;

    @Column(name = "AMOUNTTOTAL")
    private Long amountTotal;

    @Column(name = "PRODUCTNAME")
    private String productName;

    @Column(name = "PURCHASEDATE")
    private LocalDate purchaseDate;  // DATE → LocalDate 권장

    @Column(name = "TAXCODE")
    private String taxCode;
}
