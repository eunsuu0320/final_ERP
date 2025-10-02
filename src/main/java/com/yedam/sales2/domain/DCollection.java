package com.yedam.sales2.domain;

import java.util.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "COLLECTION")
public class DCollection {

	@Id
	private String moneyCode; // 수금코드
	private String key2; // 고유식별코드
	private Long totalRecpt; // 총수금
	private Long unrctBaln; //미수잔액
	private Long unrctCnt; // 미수건수
	private Date moneyDate; // 수금일자
	
	private String companyCode; // 회사고유코드
	
}
