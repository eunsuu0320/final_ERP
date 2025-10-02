package com.yedam.ac.service;

import java.util.List;

import com.yedam.ac.web.dto.AcInvoiceModalRow;

public interface AcInvoiceService {
    List<AcInvoiceModalRow> lookup(String status, String companyCode, String q, int limit);
}
