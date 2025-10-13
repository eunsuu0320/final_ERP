package com.yedam.ac.web;

import java.util.Collections;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.AcInvoiceService;
import com.yedam.ac.util.CompanyCodeProvider;
import com.yedam.ac.web.dto.AcInvoiceModalRow;

import lombok.extern.slf4j.Slf4j;

// 수금전표 조회 컨트롤러

@Slf4j
@RestController
@RequestMapping("/api/invoices")
public class AcInvoiceModalController {

    private final AcInvoiceService service;
    private final CompanyCodeProvider companyCodeProvider; // ✔ 프로젝트에 이미 존재

    public AcInvoiceModalController(AcInvoiceService service, CompanyCodeProvider companyCodeProvider) {
        this.service = service;
        this.companyCodeProvider = companyCodeProvider;
    }

    /**
     * 청구서(INVOICE) 조회 - 수금 전표용
     * 예) GET /api/invoices/lookup?status=회계반영완료&q=ABC&limit=20
  
     * @param status 
     * @param q
     * @param limit
     * @return
     */
    @GetMapping(value = "/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<AcInvoiceModalRow>> lookup(
            @RequestParam(value="status", required=false) String status,
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="limit", required=false, defaultValue="20") int limit
    ){
        String companyCode = null;
        try {
            // CompanyCodeProvider 안의 실제 메서드명에 맞춰 사용
        	companyCode = companyCodeProvider.resolveForCurrentUser(); // 없으면 null 반환하도록 구현돼 있음
        } catch (Throwable ignore) {
            // 회사코드 미설정도 허용(전사 조회) – 리포지토리에서 null 허용 처리
        	System.out.println(ignore.getMessage());
        }

        try {
            List<AcInvoiceModalRow> list = service.lookup(status, companyCode, q, limit);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            // ❗절대 500으로 죽지 않도록 방어, 로그만 남기고 빈 배열 반환
            log.error("lookup invoices failed: status={}, companyCode={}, q={}, limit={}, ex={}",
                    status, companyCode, q, limit, e.toString(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
}
