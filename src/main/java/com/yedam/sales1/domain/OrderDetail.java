package com.yedam.sales1.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.Transient;



@Entity
@NoArgsConstructor // ⭐ JPA 요구사항: 기본 생성자 (No-Argument Constructor)
@AllArgsConstructor // ⭐ @Builder 사용을 위한 모든 필드를 포함하는 생성
@Table(name = "ORDER_DETAIL")
@Data 
@Builder
public class OrderDetail {

    @Id // 기본 키 지정
    @Column(name = "ORDER_DETAIL_CODE", length = 20, nullable = false)
    private String orderDetailCode; // ORDER_DETAIL_CODE (VARCHAR2(20) NOT NULL)

    @Column(name = "ORDER_UNIQUE_CODE", nullable = false)
    private Long orderUniqueCode; // ORDER_UNIQUE_CODE (NUMBER NOT NULL)

    @Column(name = "PRODUCT_CODE", length = 20, nullable = false)
    private String productCode; // PRODUCT_CODE (VARCHAR2(20) NOT NULL)

    @Column(name = "QUANTITY")
    private Integer quantity; // QUANTITY (NUMBER NULL)

    @Column(name = "PRICE")
    private Double price; // PRICE (NUMBER NULL)

    @Column(name = "AMOUNT_SUPPLY")
    private Double amountSupply; // AMOUNT_SUPPLY (NUMBER NULL)

    @Column(name = "PCT_VAT")
    private Double pctVat; // PCT_VAT (NUMBER NULL)

    @Column(name = "STATUS", length = 20)
    private String status; // STATUS (VARCHAR2(20) NULL)

    @Column(name = "REMARKS", length = 1000)
    private String remarks; // REMARKS (VARCHAR2(1000) NULL)

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode; // COMPANY_CODE (VARCHAR2(20) NOT NULL)
    
    @Transient
    private String productName;

    @Transient
    private String productSize;

    @Transient
    private String unit;

}