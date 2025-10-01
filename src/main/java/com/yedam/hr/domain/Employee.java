package com.yedam.hr.domain;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yedam.common.domain.CommonCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
public class Employee {

	@Id
	@Column(name = "EMP_CODE")
	private String empCode; // 사원번호

	private String companyCode; // 회사코드
	private String name; // 성명
	private String phone; // 전화번호

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birth; // 생년월일
	private String email; // 이메일
	private String dept; // 부서
	private String position; // 직급
	private String grade; // 직책
	private Integer salary; // 기본급

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate hireDate; // 입사일자

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate resignDate; // 퇴사일자

	private Integer holyDays; // 휴가일수
	private Integer depCnt; // 부양가족수
	private String resignReason; // 퇴사사유
	private String bankCode; // 은행코드
	private String accHolder; // 예금주
	private String accNo; // 계좌번호
	private Integer postalCode; // 우편번호
	private String address; // 주소

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dept", referencedColumnName = "codeId", insertable = false, updatable = false)
	private CommonCode deptCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "position", referencedColumnName = "codeId", insertable = false, updatable = false)
	private CommonCode positionCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade", referencedColumnName = "codeId", insertable = false, updatable = false)
	private CommonCode gradeCode;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bankCode", referencedColumnName = "codeId", insertable = false, updatable = false)
	private CommonCode bankCodeEntity;
}
