// com.yedam.ac.domain.Partner.java
package com.yedam.ac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PARTNER")      // 스키마가 다르면 schema="YOUR_SCHEMA" 추가
@Getter @NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcPartner {

    @Id
    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "PARTNER_NAME", length = 30, nullable = false)
    private String partnerName;

    @Column(name = "PARTNER_PHONE", length = 20)
    private String partnerPhone;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;
}

//S