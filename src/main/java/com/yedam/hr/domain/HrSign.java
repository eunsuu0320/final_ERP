package com.yedam.hr.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class HrSign {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Long을 쓰면 DB의 AUTO_INCREMENT 사용
	private Long signId;

	private String companyCode;
	private String empNo;
	private String empName;
	private String empDept;
	private String img;
}
