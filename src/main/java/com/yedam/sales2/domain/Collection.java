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
@Table(name = "COLLECTION_DETAIL")
public class Collection {

	@Id
	private String collectionDetailId; // 세부수금코드
	private String moneyCode; // 수금코드
	private Long dmndAmt; // 청구금액
	private Long recpt; // 수금
	private Long delyDcnt; // 연체일수
	private String remk; // 비고
	
	private String companyCode; // 회사고유코드
	
}
