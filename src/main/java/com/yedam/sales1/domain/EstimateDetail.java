package com.yedam.sales1.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder; // EstimateServiceImpl에서 builder 패턴 사용을 위해 추가
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ESTIMATE_DETAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // EstimateServiceImpl에서 객체 복사 시 사용
public class EstimateDetail {

	@Id
	@Column(name = "ESTIMATE_DETAIL_CODE", length = 20, nullable = false)
	private String estimateDetailCode; // PK

	@Column(name = "ESTIMATE_UNIQUE_CODE", nullable = false)
	private Long estimateUniqueCode;

	// PRODUCT_CODE (VARCHAR2(20) NOT NULL) - 품목 코드
	@Column(name = "PRODUCT_CODE", length = 20, nullable = false)
	private String productCode;

	// QUANTITY (NUMBER) - 수량
	@Column(name = "QUANTITY")
	private Integer quantity;

	// PRICE (NUMBER) - 단가
	@Column(name = "PRICE")
	private Integer price;

	// REMARKS (VARCHAR2(1000)) - 비고
	@Column(name = "REMARKS", length = 1000)
	private String remarks;

	// COMPANY_CODE (VARCHAR2(20) NOT NULL) - 회사 코드
	@Column(name = "COMPANY_CODE", length = 20, nullable = false)
	private String companyCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ESTIMATE_UNIQUE_CODE", insertable = false, updatable = false)
	private Estimate estimate;
}