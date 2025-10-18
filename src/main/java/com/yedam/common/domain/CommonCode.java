package com.yedam.common.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.Data;

@JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
@Entity
@Data
public class CommonCode {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "common_code_seq")
    @SequenceGenerator(
        name = "common_code_seq",
        sequenceName = "COMMON_CODE_SEQ",
        allocationSize = 1
    )
    @Column(name = "CODE_NUM")
	private Long codeNum;
	
	private String codeId;

	private String groupId;
	private String codeName;
	private String companyCode;
	private String useYn;
}