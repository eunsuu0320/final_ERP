package com.yedam.sales2.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
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

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_plan_detail_seq")
	@SequenceGenerator(
		name = "sales_plan_detail_seq",
		sequenceName = "SALES_PLAN_DETAIL_SEQ", // DB 시퀀스명
		allocationSize = 1
	)
	private int salesPlanDetailCode; // 영업세부계획코드
	
	// 📌 중복 매핑을 일으키던 'private int salesPlanCode;' 필드를 제거했습니다.
	
	private String qtr; // 분기
	private Long purpSales; // 목표매출
	private Long purpProfitAmt; // 목표영업이익
	private Long newVendCnt; // 목표신규거래처 수
	
	// 📌 1. JPA 매핑: SALES_PLAN_CODE 컬럼과 관계를 맺습니다. (FK 소유)
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "SALES_PLAN_CODE") 
	@JsonIgnore // 순환 참조 방지
	private SalesPlan salesPlan;
	
	// 📌 2. JSON 통신용 필드: DB 쿼리에서 제외되며, PUT/POST 시 JSON 데이터 수신에 사용됩니다.
	// ServiceImpl에서 이 필드를 사용하여 SalesPlan 객체 ID를 읽고 씁니다.
	@Transient
	private Integer salesPlanCode;
	
}
