package com.yedam.sales2.domain;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class EsdpPlan implements Prefixable{

	 @Id
	    @GeneratedValue(generator = "sequence-id-generator")
	    @GenericGenerator(
	            name = "sequence-id-generator",
	            strategy = "com.yedam.common.SequenceIdGenerator"
	    )
	private String esdpCode; // 사원세부계획코드
	private String espCode; // 사원계획코드
	
	private String qtr; // 분기
	private Long purpSales; // 목표매출
	private Long purpProfitAmt; // 목표영업이익
	private Long newVendCnt; // 신규 거래처 수
	@Override
	public String getPrefix() {
		return "esdp";
	}
	@Override
	public String getSequenceName() {
		return "ESDPPLAN_SEQ";
	}
}
