package com.yedam.ac.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yedam.ac.domain.AcInvoice;
import com.yedam.ac.repository.AcInvoiceLookupRepository;
import com.yedam.ac.service.AcInvoiceService;
import com.yedam.ac.web.dto.AcInvoiceModalRow;

@Service
public class AcInvoiceServiceImpl implements AcInvoiceService {

    private final AcInvoiceLookupRepository repo;

    public AcInvoiceServiceImpl(AcInvoiceLookupRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<AcInvoiceModalRow> lookup(String status, String companyCode, String q, int limit) {
        if (status == null || status.isBlank()) status = "회계반영완료";
        if (limit <= 0 || limit > 100) limit = 20;

        // ✅ 네이티브 ROWNUM 쿼리 호출
        List<AcInvoice> rows = repo.searchAccountedTop(
                status,
                (companyCode == null || companyCode.isBlank()) ? null : companyCode,
                (q == null || q.isBlank()) ? null : q.trim(),
                limit
        );

        return rows.stream()
                .map(AcInvoiceModalRow::from)
                .toList();
    }
}
