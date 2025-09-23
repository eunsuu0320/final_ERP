package com.yedam.hr.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class HrPDF {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) // Long을 쓰면 DB의 AUTO_INCREMENT 사용
	private Long pdfKey;   // PK (자동 증가)

	private String signId; // 근로계약서 ID
	private String pdf;

}
