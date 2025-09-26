package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.repository.InvoiceRepository;
import com.yedam.sales1.service.InvoiceService;

import jakarta.transaction.Transactional;

@Service
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public List<Invoice> getAllInvoice() {
        return invoiceRepository.findAll();
    }

    @Override
    public Map<String, Object> getTableDataFromInvoice(List<Invoice> invoices) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!invoices.isEmpty()) {
            // 컬럼 정의
            columns.add("청구서코드");
            columns.add("등록일자");
            columns.add("청구일자");
            columns.add("청구금액");
            columns.add("수금일자");
            columns.add("거래처명");
            columns.add("담당자");
            columns.add("진행상태");

            for (Invoice invoice : invoices) {
                Map<String, Object> row = new HashMap<>();
                row.put("청구서코드", invoice.getInvoiceCode());
                row.put("등록일자", invoice.getCreateDate());
                row.put("청구일자", invoice.getDmndDate());
                row.put("청구금액", invoice.getDmndAmt());
                row.put("수금일자", invoice.getRecptDate());
                row.put("거래처명", invoice.getPartnerCode());
                row.put("담당자", invoice.getManager());
                row.put("진행상태", invoice.getStatus());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }


}
