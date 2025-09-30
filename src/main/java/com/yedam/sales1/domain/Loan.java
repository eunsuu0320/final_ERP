package com.yedam.sales1.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Data
@Entity
@Table(name = "LOAN") // 테이블 이름: LOAN
// LOAN_UNIQUE_CODE에 시퀀스를 사용할 경우 (Oracle 환경 가정)
@SequenceGenerator(name = "LOAN_SEQ_GEN", sequenceName = "LOAN_UNIQUE_CODE_SEQ", // DB에 정의된 시퀀스 이름
		initialValue = 1, allocationSize = 1)
public class Loan {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "LOAN_SEQ_GEN")
	@Column(name = "LOAN_UNIQUE_CODE")
	private Long loanUniqueCode; // 여신 고유 코드 (PK, 시퀀스)

	@Column(name = "LOAN_CODE")
	private String loanCode; // 여신 코드

	@Column(name = "PARTNER_CODE")
	private String partnerCode; // 거래처 코드 (FK)

	@Temporal(TemporalType.DATE)
	@Column(name = "LOAN_START_DATE")
	private Date loanStartDate; // 여신 시작일

	@Temporal(TemporalType.DATE)
	@Column(name = "LOAN_END_DATE")
	private Date loanEndDate; // 여신 종료일

	@Column(name = "LOAN_TERM")
	private String loanTerm; // 여신 기간 (기간 설정 값)

	@Column(name = "LOAN_LIMIT")
	private Long loanLimit; // 여신 한도

	@Column(name = "LOAN_USE")
	private Long loanUse; // 여신 사용액

	@Column(name = "LOAN_DAY")
	private String loanDay; // 수금/지급 예정일

	@Column(name = "MANAGER")
	private String manager; // 담당자

	@Temporal(TemporalType.DATE)
	@Column(name = "CREATE_DATE")
	private Date createDate; // 생성일자

	@Column(name = "STATUS")
	private String status; // 여신 상태

	@Column(name = "REMARKS")
	private String remarks; // 비고

	@Column(name = "VERSION")
	private Integer version; // 버전

	@Column(name = "IS_CURRENT_VERSION")
	private String isCurrentVersion; // 현재 버전 여부

	@Column(name = "COMPANY_CODE")
	private String companyCode; // 회사코드

	@PrePersist
	protected void onCreate() {
		if (this.createDate == null) {
			this.createDate = new Date();
		}
		if (this.isCurrentVersion == null) {
			this.isCurrentVersion = "Y";
		}
		if (this.version == null) {
			this.version = 1;
		}

	}

}