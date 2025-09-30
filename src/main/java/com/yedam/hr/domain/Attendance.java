package com.yedam.hr.domain;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Attendance implements Prefixable {

	@Id
	@GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
	private String attId;

    private String companyCode; // 회사코드
    private String attType; // 근태유형
    private String attIs; // 사용여부
    private String note;    // 비고


	@Override
	public String getPrefix() {
		return "ATT";
	}

	@Override
	public String getSequenceName() {
		return "ATTENDANCE_SEQ";
	}
}
