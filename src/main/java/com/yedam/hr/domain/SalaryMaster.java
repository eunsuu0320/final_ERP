package com.yedam.hr.domain;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class SalaryMaster {

	@Id
	private Long salaryId; // 급여대장 번호

	private String companyCode; // 회사고유코드
	private LocalDate payPeriod; // 귀속날짜
	private String payType; // 지급구분(공통코드 1차수 2차수)
	private String payName; // 대장 명칭
	private LocalDate payDate; // 지급일
	private LocalDate payYm; // 지급연월
	private Integer payCount; // 인원수
	private Double payTotal; // 지급총액
	private String confirmIs; // 확정여부 (공통코드 y,n)
}
