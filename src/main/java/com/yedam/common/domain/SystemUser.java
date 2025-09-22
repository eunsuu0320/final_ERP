package com.yedam.common.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
	private String empCode;
	private String userId;
	private String userPw;
	private Date createdDate;
	private String usageStatus;
	private String remk;
}
