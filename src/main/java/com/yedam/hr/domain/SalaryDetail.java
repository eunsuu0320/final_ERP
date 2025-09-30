package com.yedam.hr.domain;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "SAL_DETAIL")
public class SalaryDetail {

	@Id
	private Long salDetailId;

	@Column(name = "SALARY_ID")
	private Long salaryId;

	private String companyCode;

	@Column(name = "EMP_CODE")
	private String empCode;
	private Long salary;

	private Long all01;
	private Long all02;
	private Long all03;
	private Long all04;
	private Long all05;
	private Long all06;
	private Long all07;
	private Long all08;
	private Long all09;
	private Long all10;

	private Long allTotal;

	// 공제
	private Long ded01;
	private Long ded02;
	private Long ded03;
	private Long ded04;
	private Long ded05;
	private Long ded06;
	private Long ded07;
	private Long ded08;
	private Long ded09;
	private Long ded10;

	private Long dedTotal;
	private Long netPay;
}
