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
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "INVOICE_SEQ" 
	)
	@SequenceGenerator(name = "INVOICE_SEQ", sequenceName = "INVOICE_UNIQUE_CODE_SEQ",
			allocationSize = 1)
	@Column(name = "INVOICE_UNIQUE_CODE", length = 20, nullable = false)
	private Long invoiceUniqueCode;

	@Column(name = "INVOICE_CODE", length = 20, nullable = false)
	private String invoiceCode;

	@Column(name = "PARTNER_CODE", length = 20, nullable = false)
	private String partnerCode;

	@Column(name = "MANAGER", length = 20, nullable = false)
	private String manager;

	@Column(name = "CREATE_DATE", nullable = false)
	@Temporal(TemporalType.DATE)
	private Date createDate; // 등록일자

	@Column(name = "DMND_DATE")
	@Temporal(TemporalType.DATE)
	private Date dmndDate; // 청구일자

	@Column(name = "RECPT_DATE")
	@Temporal(TemporalType.DATE)
	private Date recptDate; // 수금일자

	@Column(name = "DMND_AMT")
	private Double dmndAmt; // 청구금액

	@Column(name = "STATUS", length = 20)
	private String status;

	@Column(name = "VERSION", nullable = false)
	private Integer version;

	@Column(name = "IS_CURRENT_VERSION", length = 10, nullable = false)
	private String isCurrentVersion; // 현재 버전여부

	@Column(name = "COMPANY_CODE", length = 20, nullable = false)
	private String companyCode;

	@Column(name = "UNRCT_BALN", nullable = false)
	private Integer unrctBaln; // 미수잔액

	@Column(name = "UPDATE_DATE")
	@Temporal(TemporalType.DATE)
	private Date updateDate; // 수정일자

	@Column(name = "PARTNER_NAME", length = 30, nullable = false)
	private String partnerName;

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
