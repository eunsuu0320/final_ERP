package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "INVOICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice {

    @Id
    @Column(name = "INVOICE_UNIQUE_CODE", length = 20, nullable = false)
    private String invoiceUniqueCode;

    @Column(name = "INVOICE_CODE", length = 20, nullable = false)
    private String invoiceCode;

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;

    @Column(name = "CREATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "DMND_DATE")
    @Temporal(TemporalType.DATE)
    private Date dmndDate;

    @Column(name = "RECPT_DATE")
    @Temporal(TemporalType.DATE)
    private Date recptDate;

    @Column(name = "DMND_AMT")
    private Double dmndAmt;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "IS_CURRENT_VERSION", length = 10, nullable = false)
    private String isCurrentVersion;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createDate == null) createDate = now;
        if (version == null) version = 1;
        if (isCurrentVersion == null) isCurrentVersion = "Y";
    }
}
