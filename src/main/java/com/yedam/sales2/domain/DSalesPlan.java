package com.yedam.sales2.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Table(name = "SALES_PLAN_DETAIL")
public class DSalesPlan {

	// 영업세부계획

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_plan_detail_seq")
	private int SalesPlanDetailCode; // 영업세부계획코드
	private String qtr; // 분기
	private Long purpSales; // 목표매출
	private Long purpProfitAmt; // 목표영업이익
	private Long newVendCnt; // 목표신규거래처 수
	
	private int salesPlanCode; // FK: SalesPlan
}
