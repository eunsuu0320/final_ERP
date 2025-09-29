// src/main/java/com/yedam/ac/domain/AcPartner.java
package com.yedam.ac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Entity
@Table(name = "PARTNER")   // 실제 테이블명
public class AcPartner extends BaseCompanyEntity { // ★ 회사코드 포함

    @Id
    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "PARTNER_NAME", length = 30, nullable = false)
    private String partnerName;

    @Column(name = "PARTNER_TYPE", length = 20)
    private String partnerType;

    @Column(name = "BUSINESS_NO", length = 20)
    private String businessNo;

    @Column(name = "CEO_NAME", length = 30)
    private String ceoName;

    @Column(name = "PARTNER_PHONE", length = 20)
    private String partnerPhone;

    @Column(name = "BUSINESS_TYPE", length = 20)
    private String businessType;

    @Column(name = "BUSINESS_SECTOR", length = 20)
    private String businessSector;

    @Column(name = "POST_CODE")
    private Integer postCode;

    @Column(name = "ADDRESS", length = 1000)
    private String address;

    @Column(name = "EMAIL", length = 300)
    private String email;

    @Column(name = "MANAGER", length = 20)
    private String manager;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "IS_PAYMENT", length = 10)
    private String isPayment;

    // CREATE_DATE, UPDATE_DATE, USAGE_STATUS 등은 필요시 추가
}
