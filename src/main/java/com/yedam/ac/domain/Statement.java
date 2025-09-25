// src/main/java/com/yedam/ac/domain/Statement.java
package com.yedam.ac.domain;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="STATEMENT")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Statement implements Persistable<String> {

    @Id
    @Column(name = "VOUCHER_NO")
    private String voucherNo;

    @Column(name = "COMPANY_CODE", nullable = false)
    private String companyCode;

    @Column(name = "VOUCHER_TYPE_CODE", nullable = false)
    private String voucherTypeCode;

    @Column(name = "VOUCHER_STATUS_CODE", nullable = false)
    @Setter
    @Builder.Default
    private String voucherStatusCode = "NORMAL";

    // ---- Persistable 구현 (insert 강제)
    @Transient
    private boolean isNew = true;

    @Override public String getId() { return voucherNo; }
    @Override public boolean isNew() { return isNew; }

    @PostLoad @PostPersist
    void markNotNew() { this.isNew = false; }
}