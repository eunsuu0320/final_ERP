package com.yedam.sales1.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "PAYMENT") // 테이블 이름: PAYMENT
public class Payment {

    @Id
    @Column(name = "PAYMENT_CODE")
    private String paymentCode;         // 결제 코드 (PK)

    @Column(name = "PARTNER_CODE")
    private String partnerCode;         // 거래처 코드 (FK)

    @Column(name = "BANK_CODE")
    private String bankCode;            // 은행 코드

    @Column(name = "BANK_NAME")
    private String bankName;            // 은행 이름

    @Column(name = "ACCOUNT_NO")
    private String accountNo;           // 계좌 번호

    @Column(name = "DEPOSITOR_NAME")
    private String depositorName;       // 예금주명

    @Column(name = "IS_DEFAULT")
    private String isDefault;           // 기본 여부

    @Column(name = "USAGE_STATUS")
    private String usageStatus;         // 사용 여부

    @Column(name = "COMPANY_CODE")
    private String companyCode;         // 회사 코드



    @PrePersist
    protected void onCreate() {

        if (this.isDefault == null) {
            this.isDefault = "Y";
        }
        if (this.usageStatus == null) {
            this.usageStatus = "Y";
        }
    }


}