package com.yedam.hr.domain;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Entity
@Data
public class Allowance implements Prefixable {
    @Id
    @GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
    private String allId; // 수당코드 (PK)

    private String companyCode; // 회사코드
    private String allName; // 수당항목
    private String formula; // 계산식
    private String calcNote; // 산출방법
    private String allIs; // 사용여부
    private int mapNum;

    @Override
    public String getPrefix() {
    	return "ALL";
    }

    @Override
    public String getSequenceName() {
    	return "ALLOWANCE_SEQ";
    }
}