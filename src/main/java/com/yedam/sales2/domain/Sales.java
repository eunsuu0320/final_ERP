package com.yedam.sales2.domain;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sales {

	@Id
	@Column(name = "SALES_CODE")
	private String saleCode;
	
	private String correspondent; // 거래처
	private String productCode; // 품목코드
	private String empCode; // 사원코드
	private String productName; // 품목명
	private Long salesQty; // 판매수량
	private Long salesPrice; // 판매단가
	private Long salesAmount; // 판매금액
	private Long costUnitPrice; // 원가단가
	private Long costAmount; // 원가금액
	private Long salesIncidentalCosts; // 판매부대비용
	private Long profitPrice; // 이익단가
	private Long profitAmount; // 이익금액
	private Date salesDate; // 판매일자
	private String companyCode; // 회사고유코드
}
