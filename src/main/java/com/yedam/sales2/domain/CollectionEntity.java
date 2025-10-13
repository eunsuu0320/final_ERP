package com.yedam.sales2.domain;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
public class CollectionEntity implements Prefixable{

	@Id
	@GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
	private String moneyCode; // 수금코드
	private Date moneyDate; // 수금일자
	
	private Long recpt; // 수금
	private String paymentMethods; // 결제방식
	private String remk; // 비고 
	
	private String companyCode; // 회사고유코드
	
	
	@Override
	public String getPrefix() {
		return "M";
	}

	@Override
	public String getSequenceName() {
		return "COLLECTION_SEQ";
	}
}
