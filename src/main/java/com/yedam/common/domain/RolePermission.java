package com.yedam.common.domain;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yedam.common.Prefixable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
@Entity
@Table(name = "ROLE_PERMISSION")
@Data
public class RolePermission implements Prefixable {

	@Id
	@GeneratedValue(generator = "sequence-id-generator")
	@GenericGenerator(name = "sequence-id-generator", strategy = "com.yedam.common.SequenceIdGenerator")
	@Column(name = "PERMISSION_CODE")
	private String permissionCode;

	@Column(name = "ROLE_CODE")
	private String roleCode;

	@Column(name = "SCREEN_CODE")
	private String screenCode;

	@Column(name = "READ_ROLE")
	private String readRole; // Y/N

	@Column(name = "CREATE_ROLE")
	private String createRole; // Y/N

	@Column(name = "UPDATE_ROLE")
	private String updateRole; // Y/N

	@Column(name = "DELETE_ROLE")
	private String deleteRole; // Y/N

	@Override
	public String getPrefix() {
		return "RP";
	}

	@Override
	public String getSequenceName() {
		return "ROLE_PERMISSION_SEQ";
	}
}