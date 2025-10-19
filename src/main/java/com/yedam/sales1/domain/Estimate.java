package com.yedam.sales1.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
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
@Table(name = "ESTIMATE")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estimate {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ESTIMATE_SEQ" // 사용할 시퀀스 이름 지정
	)
	@SequenceGenerator(name = "ESTIMATE_SEQ", sequenceName = "ESTIMATE_UNIQUE_CODE_SEQ", // DB에 생성된 시퀀스 이름과 일치해야 함
			allocationSize = 1)
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

	@Column(name = "DELIVERY_DATE")
	@Temporal(TemporalType.DATE)
	private Date deliveryDate;

	// 우편번호 (POST_CODE)
	@Column(name = "POST_CODE")
	private Integer postCode; // 또는 String

	// 주소 (ADDRESS)
	@Column(name = "ADDRESS", length = 1000)
	private String address;

	// 결제조건 (PAY_CONDITION)
	@Column(name = "PAY_CONDITION", length = 1000)
	private String payCondition;
	
	@Transient
	private String products;
	
	
	
	

	// 1. Partner 엔티티와 관계 매핑
	@ManyToOne(fetch = FetchType.LAZY) // estimate 기준으로 N:1 관계
	@JoinColumn(name = "PARTNER_CODE", referencedColumnName = "PARTNER_CODE", insertable = false, updatable = false)
	private Partner partner;
	


	// 담당자 사원 연관 (사원 테이블의 PK가 EMP_CODE라고 가정)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "MANAGER", referencedColumnName = "EMP_CODE", insertable = false, updatable = false)
	private com.yedam.hr.domain.Employee managerEmp;

	@PrePersist
	public void prePersist() {
		Date now = new Date();
		if (createDate == null)
			createDate = now;
		if (version == null)
			version = 1;
		if (isCurrentVersion == null)
			isCurrentVersion = "Y";
	}
}
