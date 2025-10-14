package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PRICE_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDetail {

    @Id
    @Column(name = "PRICE_DETAIL_CODE", nullable = false)
    private String priceDetailCode;

    @Column(name = "PRICE_UNIQUE_CODE", length = 20, nullable = false)
    private Integer priceUniqueCode;

    @Column(name = "PARTNER_CODE", length = 30)
    private String partnerCode;

    @Column(name = "PRODUCT_CODE")
    private String productCode;


    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;

    
    
    // 1. Partner 엔티티와 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY) // PRICE_DETAIL 기준으로 N:1 관계
    @JoinColumn(name = "PARTNER_CODE", referencedColumnName = "PARTNER_CODE", insertable = false, updatable = false)
    private Partner partner;

    // 2. Product 엔티티와 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY) // PRICE_DETAIL 기준으로 N:1 관계
    @JoinColumn(name = "PRODUCT_CODE", referencedColumnName = "PRODUCT_CODE", insertable = false, updatable = false)
    private Product product;

    
    // 3. Price 엔티티와 관계 매핑
    @ManyToOne(fetch = FetchType.LAZY) // PRICE_DETAIL 기준으로 N:1 관계
    @JoinColumn(name = "PRICE_UNIQUE_CODE", referencedColumnName = "PRICE_UNIQUE_CODE", insertable = false, updatable = false)
    private Price price;
    
   
}