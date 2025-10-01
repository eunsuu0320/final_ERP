package com.yedam.common.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class CommonGroup {

	@Id
	private String groupId;

	private String groupName;
	private String remk;
}
