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
@Table(name = "ESP_PLAN")
public class EspPlan {

	@Id
	private int espCode; // 사원계획코드
	
	private int spCode; // 영업계획코드
	private int empCode; // 사원코드
	private int companyCode; // 회사고유코드
}
