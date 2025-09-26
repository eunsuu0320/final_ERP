package com.yedam.sales2.domain;

import java.util.Date;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "SALES_PLAN")
public class SalesPlan {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sales_plan_seq")
	@SequenceGenerator(
		    name = "sales_plan_seq",
		    sequenceName = "SALES_PLAN_SEQ", // 실제 DB 시퀀스명
		    allocationSize = 1
		)
	private int salesPlanCode; // 영업계획코드
	
	private Date planYear; // 계획년도
	private Date regDate; // 등록일자
	private String empCode; // 사원번호
	private String companyCode; // 회사고유코드
	
	@Transient
	private List<DSalesPlan> details;
}
