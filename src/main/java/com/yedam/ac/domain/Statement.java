// src/main/java/com/yedam/ac/domain/Statement.java
package com.yedam.ac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
public class Statement {
    @Id
    @Column(name = "VOUCHER_NO")
    private String voucherNo;                // 앱에서 발번 후 세팅: setter 두지 않음

    @Column(name = "COMPANY_CODE", nullable = false)
    private String companyCode;

    @Column(name = "VOUCHER_TYPE_CODE", nullable = false)
    private String voucherTypeCode;          // SALES / BUY ...

    @Column(name = "VOUCHER_STATUS_CODE", nullable = false)
    @Setter
    @Builder.Default
    private String voucherStatusCode = "NORMAL";
}