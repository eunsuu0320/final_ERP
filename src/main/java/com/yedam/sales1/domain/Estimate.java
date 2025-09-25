package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "ESTIMATE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estimate {

    @Id
    @Column(name = "ESTIMATE_UNIQUE_CODE", nullable = false)
    private Long estimateUniqueCode;

    @Column(name = "ESTIMATE_CODE", length = 20, nullable = false)
    private String estimateCode;

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "CREATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "EXPIRY_DATE", length = 20)
    private String expiryDate;

    @Column(name = "TOTAL_AMOUNT")
    private Double totalAmount;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "IS_CURRENT_VERSION", length = 10, nullable = false)
    private String isCurrentVersion;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

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
