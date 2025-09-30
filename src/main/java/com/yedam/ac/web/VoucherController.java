// src/main/java/com/yedam/ac/web/VoucherController.java
package com.yedam.ac.web;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.VoucherNoService;
import com.yedam.ac.util.CompanyContext;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/vouchers")
public class VoucherController {

    private final CompanyContext companyContext;
    private final VoucherNoService voucherNoService;

    /** A) 프리뷰 — 화면 표시 용 (DB 변경 없음 / 번호 안 올라감) */
    @GetMapping("/preview")
    public Map<String, String> preview(
            @RequestParam String type, // SALES | BUY | MONEY | PAYMENT
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String cc = companyContext.getCompanyCode();
        if (cc == null) throw new IllegalStateException("회사코드 누락");

        String kind = normalize(type);
        String voucherNo = voucherNoService.previewNext(kind, date);
        return Map.of("voucherNo", voucherNo);
    }

    /** B) 예약 — 저장 직전(확정 발번) */
    @GetMapping("/reserve")
    public Map<String, String> reserve(
            @RequestParam String type, // SALES | BUY | MONEY | PAYMENT
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        String cc = companyContext.getCompanyCode();
        if (cc == null) throw new IllegalStateException("회사코드 누락");

        String kind = normalize(type);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = (auth != null ? auth.getName() : "anonymous");

        var res = voucherNoService.reserve(kind, date, userId);
        return Map.of("reservationId", res.reservationId(), "voucherNo", res.voucherNo());
    }

    private static String normalize(String type) {
        return switch (String.valueOf(type).toUpperCase()) {
            case "SALES"   -> "SALES";
            case "BUY"     -> "BUY";
            case "MONEY"   -> "MONEY";
            case "PAYMENT" -> "PAYMENT";
            default -> throw new IllegalArgumentException("unknown type: " + type);
        };
    }
}
