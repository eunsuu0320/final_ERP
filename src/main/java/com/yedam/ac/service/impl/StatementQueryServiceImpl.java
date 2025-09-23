package com.yedam.ac.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.yedam.ac.repository.StatementQueryRepository;
import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementQueryServiceImpl implements StatementQueryService {

    private final StatementQueryRepository repository;

    @Override
    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
        int page = form.getPage() == null ? 0 : Math.max(form.getPage(), 0);
        int size = form.getSize() == null ? 10 : Math.max(form.getSize(), 1);
        int start = page * size;
        int end   = start + size;
        Pageable pageable = PageRequest.of(page, size);

        String type = StringUtils.hasText(form.getType()) ? form.getType().toUpperCase() : "ALL";
        String keyword = StringUtils.hasText(form.getKeyword()) ? form.getKeyword().trim() : null;
        String voucherNo = StringUtils.hasText(form.getVoucherNo()) ? form.getVoucherNo().trim() : null;

        long total = repository.countUnified(
                type, keyword, voucherNo, form.getFromDate(), form.getToDate());

        List<UnifiedStatementRow> content = repository.searchUnifiedList(
                type, keyword, voucherNo, form.getFromDate(), form.getToDate(),
                start, end);

        return new PageImpl<>(content, pageable, total);
    }
}
