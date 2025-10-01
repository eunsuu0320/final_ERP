package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder; // EstimateServiceImpl에서 builder 패턴 사용을 위해 추가

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
}