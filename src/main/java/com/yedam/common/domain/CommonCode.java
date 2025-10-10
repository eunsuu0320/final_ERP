package com.yedam.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Data
public class CommonCode {

	@Id
	private String codeId;

	private String groupId;
	private String codeName;
	private String companyCode;
}
