package com.yedam.hr.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@Entity
@Data
public class Dedcut {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ded_seq")
    @SequenceGenerator(name = "ded_seq", sequenceName = "DED_ID_SEQ", allocationSize = 1)
    private Long dedId;   // 공제코드 (PK)

    private String companyCode;   // 회사고유코드
    private String dedName;   // 공제항목
    private String formula;   // 계산식
    private String calcNote;   // 산출방법
    private String allIs;   // 사용여부
}