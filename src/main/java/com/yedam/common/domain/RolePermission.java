package com.yedam.common.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yedam.common.Prefixable;
import com.yedam.hr.domain.Employee;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Entity
@Table(name = "ROLE_PERMISSION")
@Data
public class RolePermission implements Prefixable {
	
	@Id
	private String permissionCode;
	private String moduleCode;
	private String screenCode;
	private String screenName;
	// 체크박스는 'Y'면 체크, 'N'이면 해제
	private String readYn; // 'Y' or 'N'
	private String createYn; // 'Y' or 'N'
	private String updateYn; // 'Y' or 'N'
	private String deleteYn; // 'Y' or 'N'
	
	@Override
	public String getPrefix() {
		return "RP";
	}

	@Override
	public String getSequenceName() {
		return "ROLE_PERMISSION_SEQ";
	}
}
