package com.yedam.common.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class CommonCode {

	@Id
	private String codeId;

	private String groupId;
	private String codeName;
	private String companyCode;
}
