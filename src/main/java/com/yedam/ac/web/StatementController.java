package com.yedam.ac.web;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.StatementQueryService;      // 추가
import com.yedam.ac.service.StatementService;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statements")
public class StatementController {
    private final StatementService svc;
    private final StatementQueryService queryService;  // 추가

    // ▼ 목록 조회 (Tabulator가 호출)
    @GetMapping
    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
        return queryService.search(form);
    }

    // ▼ 등록(매출)
    @PostMapping("/sales")
    public ResponseEntity<StatementCreateRes> createSales(@RequestBody SalesCreateReq req) {
        return ResponseEntity.ok(svc.createSalesStatement(req));
    }

    // ▼ 등록(매입)
    @PostMapping("/buy")
    public ResponseEntity<StatementCreateRes> createBuy(@RequestBody BuyCreateReq req) {
        return ResponseEntity.ok(svc.createBuyStatement(req));
    }
}