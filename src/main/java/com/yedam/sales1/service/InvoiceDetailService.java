package com.yedam.sales1.service;

import java.util.List;

import com.yedam.sales1.domain.InvoiceDetail;

public interface InvoiceDetailService {
	List<InvoiceDetail> getAllInvoiceDetail();
	
	InvoiceDetail saveInvoiceDetail(InvoiceDetail invoiceDetail);
}
