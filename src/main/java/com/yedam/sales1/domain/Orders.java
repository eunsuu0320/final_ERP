package com.yedam.sales1.domain;

import jakarta.persistence.*; // jakarta.persistence.* 임포트 유지
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SequenceGenerator(
    name = "ORDER_SEQ_GENERATOR", 
    sequenceName = "ORDER_SEQ",   
    initialValue = 1,             
    allocationSize = 1            
)
public class Orders {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE, 
        generator = "ORDER_SEQ_GENERATOR" 
    )
    @Column(name = "ORDER_UNIQUE_CODE", nullable = false)
    private Long orderUniqueCode; 

    @Column(name = "ESTIMATE_UNIQUE_CODE", nullable = false)
    private Long estimateUniqueCode;

    @Column(name = "ORDER_CODE", length = 20, nullable = false)
    private String orderCode; 

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "CREATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "TOTAL_AMOUNT")
    private Double totalAmount;

    @Column(name = "DELIVERY_DATE")
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

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

    
 // 거래처 연관 (복합키 조인)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
      @JoinColumn(name = "PARTNER_CODE", referencedColumnName = "PARTNER_CODE", insertable = false, updatable = false),
      @JoinColumn(name = "COMPANY_CODE", referencedColumnName = "COMPANY_CODE", insertable = false, updatable = false)
    })
    private Partner partner;
    
    
    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createDate == null) createDate = now;
        if (version == null) version = 1;
        if (isCurrentVersion == null) isCurrentVersion = "Y";
    }
}