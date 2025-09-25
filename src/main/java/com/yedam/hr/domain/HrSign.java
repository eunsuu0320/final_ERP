package com.yedam.hr.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Entity
@Data
public class HrSign {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hr_sign_seq")
	@SequenceGenerator(name = "hr_sign_seq", sequenceName = "HR_SIGN_SEQ", allocationSize = 1)
	private Long signId;

	private String companyCode;
	private String empNo;
	private String empName;
	private String empDept;
	private String img;
}
