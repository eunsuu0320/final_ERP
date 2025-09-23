package com.yedam.domain;

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
	private String salesPlanCode;
	
	private Date planYear;
	private Date regDate;
	private String empCode;
	private String companyCode;
	
	private String salesPaneDetailCode;
	private String qtr;
	private Long purpSales;
	private Long purpProfitAmt;
	private Long newVendCnt;
	
	
}
