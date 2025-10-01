package com.yedam.hr.domain;

import java.time.LocalDate;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class EmpAttendance implements Prefixable {

	  // 사원 근태 번호 (PK)
    @Id @GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
    private String empAttId;

    private String attId;     // 근태코드
    private Integer companyCode;     // 회사코드
    private Integer empCode;     // 사원번호
    private Integer workTime;   // 근태 시간
    private LocalDate workDate;    // 근태일자
    private String holyIs; // 휴가여부
    private String note;    // 비고


    @Override
    public String getPrefix() {
    	return "EAT";
    }

    @Override
    public String getSequenceName() {
    	return "EMP_ATTENDANCE_SEQ";
    }
}
