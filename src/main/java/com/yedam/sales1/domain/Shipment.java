package com.yedam.sales1.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SHIPMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @Column(name = "SHIPMENT_CODE", length = 20, nullable = false)
    private String shipmentCode;

    @Column(name = "SHIPMENT_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date shipmentDate;

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "WAREHOUSE", length = 30)
    private String warehouse;
    
    
    @Column(name = "IS_INVOICE", length = 30)
    private String isInvoice;

    @Column(name = "TOTAL_QUANTITY")
    private Integer totalQuantity;

    @Column(name = "CREATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "UPDATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date updateDate;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;

    @Column(name = "POST_CODE")
    private Integer postCode;


    @Column(name = "ADDRESS", length = 1000)
    private String address;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
    
    
	// 1. Partner 엔티티와 관계 매핑
	@ManyToOne(fetch = FetchType.LAZY) // estimate 기준으로 N:1 관계
	@JoinColumn(name = "PARTNER_CODE", referencedColumnName = "PARTNER_CODE", insertable = false, updatable = false)
	private Partner partner;
	


	// 담당자 사원 연관 (사원 테이블의 PK가 EMP_CODE라고 가정)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MANAGER", referencedColumnName = "EMP_CODE", insertable = false, updatable = false)
	private com.yedam.hr.domain.Employee managerEmp;
}
