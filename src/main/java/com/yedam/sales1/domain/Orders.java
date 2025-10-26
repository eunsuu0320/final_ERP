package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Table(name = "ORDERS")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@SequenceGenerator(name = "ORDER_SEQ_GENERATOR", sequenceName = "ORDER_SEQ", initialValue = 1, allocationSize = 1)
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ORDER_SEQ_GENERATOR")
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
    
    @Column(name = "WRITER", length = 20, nullable = false)
    private String writer;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "VERSION", nullable = false)
    private Integer version;
    
    @Column(name = "PARTNER_DISCOUNT_AMOUNT")
    private Integer partnerDiscountAmount;

    @Column(name = "IS_CURRENT_VERSION", length = 10, nullable = false)
    private String isCurrentVersion;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
    
	// 우편번호 (POST_CODE)
	@Column(name = "POST_CODE")
	private Integer postCode; // 또는 String

	// 주소 (ADDRESS)
	@Column(name = "ADDRESS", length = 1000)
	private String address;

	// 결제조건 (PAY_CONDITION)
	@Column(name = "PAY_CONDITION", length = 1000)
	private String payCondition;
	


    // 거래처 연관 (복합키)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "PARTNER_CODE", referencedColumnName = "PARTNER_CODE", insertable = false, updatable = false),
        @JoinColumn(name = "COMPANY_CODE", referencedColumnName = "COMPANY_CODE", insertable = false, updatable = false)
    })
    private Partner partner;

    // ✅ 담당자 사원 연관 (estimate와 동일 스타일)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MANAGER", referencedColumnName = "EMP_CODE", insertable = false, updatable = false)
    private com.yedam.hr.domain.Employee managerEmp;
    


    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createDate == null) createDate = now;
        if (version == null) version = 1;
        if (isCurrentVersion == null) isCurrentVersion = "Y";
        if (status == null) status = "미확인";
    }
}
