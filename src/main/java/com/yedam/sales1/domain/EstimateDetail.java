package com.yedam.sales1.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ESTIMATE_DETAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstimateDetail implements java.io.Serializable {

	private static final long serialVersionUID = 1L;

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
	
	
	@Transient
	private String productName;

	@Transient
	private String productSize;

	@Transient
	private String unit;

	// ✅ 수정 코드 (순환 참조 완전 차단)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ESTIMATE_UNIQUE_CODE", insertable = false, updatable = false)
	private Estimate estimate;


	// ✅ 상품(Product)과의 연관관계 추가
	@JsonIgnore
	@JsonManagedReference
	@EqualsAndHashCode.Exclude
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "PRODUCT_CODE", insertable = false, updatable = false)
	private Product product;  // Product 엔티티 참조
}
