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
@Table(name = "PARTNER")
public class Partner {

	@Id
	@Column(name = "PARTNER_CODE")
	private String partnerCode; // 거래처 코드

	@Column(name = "PARTNER_NAME")
	private String partnerName; // 거래처명

	@Column(name = "PARTNER_TYPE")
	private String partnerType; // 거래처 유형

	@Column(name = "BUSINESS_NO")
	private String businessNo; // 사업자 번호

	@Column(name = "CEO_NAME")
	private String ceoName; // 대표자명

	@Column(name = "PARTNER_PHONE")
	private String partnerPhone; // 전화번호

	@Column(name = "BUSINESS_TYPE")
	private String businessType; // 업종

	@Column(name = "BUSINESS_SECTOR")
	private String businessSector; // 업태

	@Column(name = "POST_CODE")
	private Integer postCode; // 우편번호

	@Column(name = "ADDRESS")
	private String address; // 주소

	@Column(name = "EMAIL")
	private String email; // 이메일

	@Column(name = "MANAGER")
	private String manager; // 담당자

	@Column(name = "REMARKS")
	private String remarks; // 비고

	@Column(name = "IS_PAYMENT")
	private String isPayment; // 결제여부

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATE_DATE")
	private Date createDate; // 생성일자

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "UPDATE_DATE")
	private Date updateDate; // 수정일자

	@Column(name = "USAGE_STATUS")
	private String usageStatus; // 사용여부

	@Column(name = "COMPANY_CODE")
	private String companyCode; // 회사코드

	@PrePersist
	protected void onCreate() {
		this.createDate = new Date();
		this.updateDate = new Date();

		if (this.usageStatus == null) {
			this.usageStatus = "Y";
		}
	}

	@PreUpdate
	protected void onUpdate() {
		this.updateDate = new Date();
	}
}
