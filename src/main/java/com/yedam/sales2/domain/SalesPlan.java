package com.yedam.sales2.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class SalesPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private String salesPlanCode; // 영업계획코드
	
	private Date planYear; // 계획년도
	private Date regDate; // 등록일자
	private String empCode; // 사원번호
	private String companyCode; // 회사고유코드
	
	
	//영업세부계획
	private String SalesPlanDetailCode; // 영업세부계획코드
	private String qtr; // 분기
	private Long purpSales; // 목표매출
	private Long prupProfitAmt; // 목표영업이익
	private Long newvendCnt; // 목표신규거래처 수 
	
}
