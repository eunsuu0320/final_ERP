// src/main/java/com/yedam/ac/service/impl/StatementQueryServiceImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.yedam.ac.repository.StatementQueryRepository;
import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementQueryServiceImpl implements StatementQueryService {

    private final StatementQueryRepository repo;

    @Override
    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
        final String type      = (form.getType() == null || form.getType().isBlank()) ? "ALL" : form.getType();
        final String keyword   = form.getKeyword()   == null ? "" : form.getKeyword().trim();   // ✅ keyword 사용
        final String voucherNo = form.getVoucherNo() == null ? "" : form.getVoucherNo().trim();

        final LocalDate fromDate = form.getFromDate();
        final LocalDate toDate   = form.getToDate();

        final int page  = form.getPage() == null ? 0  : Math.max(0, form.getPage());
        final int size  = form.getSize() == null ? 20 : Math.max(1, form.getSize());
        final int start = page * size;
        final int end   = start + size;

        List<UnifiedStatementRow> rows = repo.searchUnifiedList(
                type, keyword, voucherNo, fromDate, toDate, start, end
        );
        long total = repo.countUnified(type, keyword, voucherNo, fromDate, toDate);

        return new PageImpl<>(rows, PageRequest.of(page, size), total);
    }
}