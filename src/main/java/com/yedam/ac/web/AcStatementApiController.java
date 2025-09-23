package com.yedam.ac.web;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AcStatementApiController {

    private final StatementQueryService queryService;

    // Tabulator 원격 페이징 데이터 소스
    @GetMapping("/api/statements")
    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
        // Tabulator는 page를 1-base로 보낼 수 있으므로 ajaxURLGenerator에서 0-base로 맞춰 보냄.
        // 여기서는 form.page가 이미 0-base라고 가정 (프런트에서 처리)
        return queryService.search(form);
    }
}
