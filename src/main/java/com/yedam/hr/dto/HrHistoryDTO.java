package com.yedam.hr.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HrHistoryDTO {

	private String companyCode;
	private String eventType;
	private String eventDetail;
	private String manager;
	private Date createdAt;
	private String empCode;
	private String name; // 성명

}
