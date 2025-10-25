package com.yedam.sales1.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Column(name = "DISCOUNT_PCT")
    private Double discountPct;

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
    
    @Column(name = "START_DATE")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    
    @Column(name = "END_DATE")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    
    
    
    
    
    @Transient
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date validDate;
    
    @JsonIgnore
    @OneToMany(mappedBy = "price", fetch = FetchType.LAZY) 
    private List<PriceDetail> priceDetails = new ArrayList<>();

    
    

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