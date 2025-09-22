package com.yedam.hr.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Employees {

	@Id
	private String empNo; // 사원번호

	private String companyCode; // 회사코드
	private String name; // 성명
	private String phone; // 전화번호
	private Date birth; // 생년월일
	private String email; // 이메일
	private String dept; // 부서
	private String grade; // 직급
	private String position; // 직책
	private int salary; // 기본급
	private Date hireDate; // 입사일자
	private Date resignDate; // 퇴사일자
	private int holyDays; // 휴가일수
	private int depCnt; // 부양가족수
	private String resignReason; // 퇴사사유
	private String bankCode; // 은행코드
	private String accHolder; // 예금주
	private String accNo; // 계좌번호
	private String postalCode; // 우편번호
	private String address; // 주소
}
