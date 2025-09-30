package com.yedam.common.domain;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Company implements Prefixable {
	@Id
    @GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
	private String attId;

	private String companyCode;

	private String companyName;	  // 회사명
	private String ceoName;       // 대표자명
    private String bizRegNo;      // 사업자 등록번호
    private String roadAddress;   // 주소
    private String addressDetail; // 상세 주소
    private String tel;           // 담당자 연락처

	@Override
	public String getPrefix() {
		return "C";
	}
	@Override
	public String getSequenceName() {
		return "COMPANY_SEQ";
	}
}
