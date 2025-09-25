package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "PRICE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Price {

    @Id
    @Column(name = "PRICE_UNIQUE_CODE", nullable = false)
    private Long priceUniqueCode;

    @Column(name = "PRICE_GROUP_CODE", length = 20, nullable = false)
    private String priceGroupCode;

    @Column(name = "PRICE_GROUP_NAME", length = 30)
    private String priceGroupName;

    @Column(name = "PRICE")
    private Double price;

    @Column(name = "PRICE_TYPE", length = 20)
    private String priceType;

    @Column(name = "CREATE_DATE")
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "USAGE_STATUS", length = 10)
    private String usageStatus;

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
        if (createDate == null) {
            createDate = new Date();
        }
        if (usageStatus == null) {
        	usageStatus = "Y";
        }
        if (version == null) {
            version = 1;
        }
        if (isCurrentVersion == null) {
            isCurrentVersion = "Y";
        }
    }
}