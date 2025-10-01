// src/main/java/com/yedam/ac/web/GeneralStatementController.java
package com.yedam.ac.web;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.GeneralVoucherService;
import com.yedam.ac.web.dto.MoneyReq;
import com.yedam.ac.web.dto.PaymentReq;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/statements")
public class GeneralStatementController {

    private final GeneralVoucherService svc;

    @PostMapping("/money")
    public ResponseEntity<Map<String,String>> saveMoney(@RequestBody MoneyReq req){
        String vno = svc.saveReceipt(req);
        return ResponseEntity.ok(Map.of("voucherNo", vno));
    }

    @PostMapping("/payment")
    public ResponseEntity<Map<String,String>> savePayment(@RequestBody PaymentReq req){
        String vno = svc.savePayment(req);
        return ResponseEntity.ok(Map.of("voucherNo", vno));
    }
}
