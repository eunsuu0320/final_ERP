package com.yedam.common.domain;

import java.util.Date;

import com.yedam.hr.domain.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "SYSTEM_USER")
@Data
public class SystemUser {

	@Id
	private String userCode;

	private String companyCode;
	private String roleCode;

	@Column(name = "EMP_CODE")
	private String empCode;

	private String userId;
	private String userPw;
	private Date createdDate;
	private String usageStatus;
	private String remk;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_CODE", insertable = false, updatable = false)

    private Employee employee;
}
