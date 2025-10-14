package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.dto.InvoiceRegistrationDTO;

public interface InvoiceService {
	List<Invoice> getAllInvoice();

	Map<String, Object> getTableDataFromInvoice(List<Invoice> invoices);

	Invoice saveInvoice(Invoice invoices);
	
	Long registerNewInvoice(InvoiceRegistrationDTO dto);

	
	boolean updateInvoiceStatus(String invoiceCode, String status);

}
