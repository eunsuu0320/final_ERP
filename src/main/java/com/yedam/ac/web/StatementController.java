// src/main/java/com/yedam/ac/web/StatementController.java
package com.yedam.ac.web;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.service.StatementService;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.MoneyReq;
import com.yedam.ac.web.dto.PaymentReq;
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
    private final StatementQueryService queryService;

    /** 목록 조회 */
    @GetMapping
    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
        return queryService.search(form);
    }

    /** 매출 */
    @PostMapping("/sales")
    public ResponseEntity<StatementCreateRes> createSales(@RequestBody SalesCreateReq req) {
        return ResponseEntity.ok(svc.createSalesStatement(req));
    }

    /** 매입 */
    @PostMapping("/buy")
    public ResponseEntity<StatementCreateRes> createBuy(@RequestBody BuyCreateReq req) {
        return ResponseEntity.ok(svc.createBuyStatement(req));
    }

    /** 수금 */
    @PostMapping("/money")
    public ResponseEntity<StatementCreateRes> createMoney(@RequestBody MoneyReq req) {
        return ResponseEntity.ok(svc.createMoneyStatement(req));
    }

    /** 지급 */
    @PostMapping("/payment")
    public ResponseEntity<StatementCreateRes> createPayment(@RequestBody PaymentReq req) {
        return ResponseEntity.ok(svc.createPaymentStatement(req));
    }
}
