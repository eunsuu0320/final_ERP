package com.yedam.hr.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "HR_PDF")   // DB 실제 테이블명 지정
public class HrPDF {

	@Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "hr_pdf_seq")
    @SequenceGenerator(name = "hr_pdf_seq", sequenceName = "HR_PDF_SEQ", allocationSize = 1)
    private Long pdfKey;   // PK (자동 증가)

	private Long signId; // 근로계약서 ID
	private String pdf;

}
