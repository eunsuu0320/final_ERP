package com.yedam.hr.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yedam.hr.domain.Employee;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

	private String companyCode; // 회사코드
	private String name; // 성명
	private String phone; // 전화번호
	private String email; // 이메일

	// 공통코드 codeId 그대로 전달
	private String dept; // 부서 코드
	private String position; // 직급 코드
	private String grade; // 직책 코드

	private Integer salary; // 기본급

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birth; // 생년월일

	// 폼 전송 호환을 위해 String → 저장 시 LocalDate로 변환
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String hireDate; // 입사일자(yyyy-MM-dd)

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private String resignDate; // 퇴사일자(yyyy-MM-dd, 선택)

	private Integer holyDays; // 휴가일수
	private Integer depCnt; // 부양가족수
	private String resignReason; // 퇴사사유

	private String bankCode; // 은행 코드 (공통코드)
	private String accHolder; // 예금주
	private String accNo; // 계좌번호

	private Integer postalCode; // 우편번호
	private String address; // 주소

	/** DTO → Entity 변환 (empCode는 자동 생성) */
	public Employee toEntity() {
		Employee e = new Employee();

		e.setCompanyCode(this.companyCode != null ? this.companyCode.trim() : null);
		e.setName(this.name != null ? this.name.trim() : null);
		e.setPhone(this.phone != null ? this.phone.trim() : null);
		e.setEmail(this.email != null ? this.email.trim() : null);

		e.setDept(this.dept != null ? this.dept.trim() : null);
		e.setPosition(this.position != null ? this.position.trim() : null);
		e.setGrade(this.grade != null ? this.grade.trim() : null);

		e.setSalary(this.salary);
		e.setBirth(this.birth);

		e.setHireDate(
				(this.hireDate != null && !this.hireDate.isBlank()) ? LocalDate.parse(this.hireDate.trim()) : null);
		e.setResignDate(
				(this.resignDate != null && !this.resignDate.isBlank()) ? LocalDate.parse(this.resignDate.trim())
						: null);

		e.setHolyDays(this.holyDays);
		e.setDepCnt(this.depCnt);
		e.setResignReason(this.resignReason != null ? this.resignReason.trim() : null);

		e.setBankCode(this.bankCode != null ? this.bankCode.trim() : null);
		e.setAccHolder(this.accHolder != null ? this.accHolder.trim() : null);
		e.setAccNo(this.accNo != null ? this.accNo.trim() : null);

		e.setPostalCode(this.postalCode);
		e.setAddress(this.address != null ? this.address.trim() : null);

		return e;
	}
}
