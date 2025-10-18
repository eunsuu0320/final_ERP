package com.yedam.sales1.dto;

import java.util.Date;
import java.util.List; // 리스트 임포트 필수

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.InvoiceDetail;

import lombok.Data;

@Data
public class InvoiceRegistrationDTO {
	// 1. invoice
    private String partnerCode;
    private String partnerName;
    private String manager; 
    private Date dmndDate; 
    private Double dmndAmt;
    private String status;

    // 2. invoiceDetail
    private List<InvoiceDetail> InvoiceDetail; 
}