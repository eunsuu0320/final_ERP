package com.yedam.hr.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
public class SalaryDetail {
	@Id
	@Column(name = "SAL_DETAIL_ID")
	private Long salDetailId;

	@Column(name = "SALARY_ID")
	private String salaryId;

	@Column(name = "COMPANY_CODE")
	private String companyCode;

	@Column(name = "EMP_CODE")
	private String empCode;

	@Column(name = "SALARY")
	private Long salary;

	// 지급
	@Column(name = "ALL_01")
	private Long all01;
	@Column(name = "ALL_02")
	private Long all02;
	@Column(name = "ALL_03")
	private Long all03;
	@Column(name = "ALL_04")
	private Long all04;
	@Column(name = "ALL_05")
	private Long all05;
	@Column(name = "ALL_06")
	private Long all06;
	@Column(name = "ALL_07")
	private Long all07;
	@Column(name = "ALL_08")
	private Long all08;
	@Column(name = "ALL_09")
	private Long all09;
	@Column(name = "ALL_10")
	private Long all10;

	@Column(name = "ALL_TOTAL")
	private Long allTotal;

	// 공제
	@Column(name = "DED_01")
	private Long ded01;
	@Column(name = "DED_02")
	private Long ded02;
	@Column(name = "DED_03")
	private Long ded03;
	@Column(name = "DED_04")
	private Long ded04;
	@Column(name = "DED_05")
	private Long ded05;
	@Column(name = "DED_06")
	private Long ded06;
	@Column(name = "DED_07")
	private Long ded07;
	@Column(name = "DED_08")
	private Long ded08;
	@Column(name = "DED_09")
	private Long ded09;
	@Column(name = "DED_10")
	private Long ded10;

	@Column(name = "DED_TOTAL")
	private Long dedTotal;

	@Column(name = "NET_PAY")
	private Long netPay;
}
