package com.yedam.hr.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Entity
@Data
public class Allowance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "allowance_seq")
	@SequenceGenerator(name = "allowance_seq", sequenceName = "ALLOWANCE_SEQ", allocationSize = 1)
    private Long allId; // 수당코드 (PK)

    private String companyCode; // 회사코드
    private String allName; // 수당항목
    private String formula; // 계산식
    private String calcNote; // 산출방법
    private String allIs; // 사용여부
}