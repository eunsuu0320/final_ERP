package com.yedam.sales2.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ESDP_PLAN")
public class EsdpPlan {

	@Id
	private int esdpCode; // 사원세부계획코드
	private int espCode; // 사원계획코드
	
	private String qtr; // 분기
	private Long vendCnt; // 거래처 수
	private Long purpSales; // 목표매출
	private Long purpProfitAmt; // 목표영업이익
	private Long newVendCnt; // 신규 거래처 수
}
