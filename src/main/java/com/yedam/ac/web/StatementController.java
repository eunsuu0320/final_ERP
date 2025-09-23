package com.yedam.ac.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.StatementService;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statements")
public class StatementController {
    private final StatementService svc;

    @PostMapping("/sales")
    public ResponseEntity<StatementCreateRes> createSales(@RequestBody SalesCreateReq req) {
        return ResponseEntity.ok(svc.createSalesStatement(req));
    }

    @PostMapping("/buy")
    public ResponseEntity<StatementCreateRes> createBuy(@RequestBody BuyCreateReq req) {
        return ResponseEntity.ok(svc.createBuyStatement(req));
    }
}