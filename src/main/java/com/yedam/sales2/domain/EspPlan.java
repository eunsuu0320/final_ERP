package com.yedam.sales2.domain;

import java.util.List;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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

public class EspPlan implements Prefixable{

	@Id
    @GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
	private String espCode; // 사원계획코드
	
	private int spCode; // 영업계획코드
	private int empCode; // 사원코드
	private int companyCode; // 회사고유코드
	
	
	@Override
	public String getPrefix() {
		return "esp";
	}
	@Override
	public String getSequenceName() {
		return "ESPPLAN_SEQ";
	}
	
	@OneToMany(mappedBy = "espCode", cascade = CascadeType.ALL)
	private List<EsdpPlan> detailPlans;
}
