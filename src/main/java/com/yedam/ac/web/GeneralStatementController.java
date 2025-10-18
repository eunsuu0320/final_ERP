//// src/main/java/com/yedam/ac/web/GeneralStatementController.java
//package com.yedam.ac.web;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.yedam.ac.service.StatementService;
//import com.yedam.ac.web.dto.MoneyReq;
//import com.yedam.ac.web.dto.PaymentReq;
//import com.yedam.ac.web.dto.StatementCreateRes;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//@RequestMapping("/api/statements")
//public class GeneralStatementController {
//
//    private final StatementService svc;
//
//    /** 수금 저장 (기존 URL 유지) */
//    @PostMapping("/money")
//    public ResponseEntity<StatementCreateRes> saveMoney(@RequestBody MoneyReq req) {
//        return ResponseEntity.ok(svc.createMoneyStatement(req));
//    }
//
//    /** 지급 저장 (기존 URL 유지) */
//    @PostMapping("/payment")
//    public ResponseEntity<StatementCreateRes> savePayment(@RequestBody PaymentReq req) {
//        return ResponseEntity.ok(svc.createPaymentStatement(req));
//    }
//}
