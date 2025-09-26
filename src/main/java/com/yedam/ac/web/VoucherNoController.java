// src/main/java/com/yedam/ac/web/VoucherNoController.java
package com.yedam.ac.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.VoucherNoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VoucherNoController {

    private final VoucherNoService voucherNoService;

    /**
     * 예: /api/vouchers/next?type=SALES&date=2025-09-25
     *     /api/vouchers/next?type=BUY   (date 생략 시 오늘)
     */
    @GetMapping("/api/vouchers/next")
    public NextVoucherNoRes next(
        @RequestParam("type") String type,
        @RequestParam(value = "date", required = false)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        String next = voucherNoService.next(type, date);
        return new NextVoucherNoRes(type.toUpperCase(), next);
    }

    public record NextVoucherNoRes(String type, String voucherNo) {}
}
