package com.yedam.ac.util;

import java.time.LocalDate;

import org.springframework.stereotype.Component;

import com.yedam.ac.service.VoucherNoService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class VoucherNoGenerator {

    private final VoucherNoService voucherNoService;

    /** type 기준 프리뷰 번호(yymm-####) 반환 — DB 변경 없음 */
    public String next(String type, LocalDate voucherDate) {
        return voucherNoService.previewNext(type, voucherDate);
    }

    // 편의 메서드 (기존 호출부와의 호환용)
    public String nextSales(LocalDate d)   { return next("SALES", d); }
    public String nextBuy(LocalDate d)     { return next("BUY", d); }
    public String nextPayment(LocalDate d) { return next("PAYMENT", d); }
    public String nextMoney(LocalDate d)   { return next("MONEY", d); }
}
