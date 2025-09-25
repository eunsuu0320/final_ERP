package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Invoice;

public interface InvoiceService {
	List<Invoice> getAllInvoice();

	Map<String, Object> getTableDataFromInvoice(List<Invoice> invoices);

	Invoice saveInvoice(Invoice invoices);
}
