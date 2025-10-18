// src/main/java/com/yedam/ac/service/impl/VoucherNoServiceImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.yedam.ac.repository.VoucherPreviewRepository;
import com.yedam.ac.repository.VoucherReservationRepository;
import com.yedam.ac.repository.VoucherReservationRepository.ReserveRes;
import com.yedam.ac.service.VoucherNoService;
import com.yedam.ac.util.CompanyContext;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherNoServiceImpl implements VoucherNoService {

    private final CompanyContext companyCtx;
    private final VoucherPreviewRepository previewRepo;
    private final VoucherReservationRepository reservationRepo;

    private static String yymm(LocalDate d) {
        int y = d.getYear() % 100;
        int m = d.getMonthValue();
        return String.format("%02d%02d", y, m);
    }

    private static String incFromPrefix(String maxVoucherNoOrNull, String prefix) {
        if (maxVoucherNoOrNull == null || maxVoucherNoOrNull.isBlank()) return prefix + "0001";
        int pos = maxVoucherNoOrNull.indexOf('-');
        String seqPart = (pos >= 0) ? maxVoucherNoOrNull.substring(pos + 1) : "0000";
        int seq = 0;
        try { seq = Integer.parseInt(seqPart); } catch (Exception ignore) {}
        return prefix + String.format("%04d", seq + 1);
    }

    @Override
    public String previewNext(String kind, LocalDate baseDate) {
        String cc = companyCtx.getCompanyCode();
        if (cc == null || cc.isBlank()) throw new IllegalStateException("회사코드 세션 누락");

        LocalDate d = (baseDate != null ? baseDate : LocalDate.now());
        String prefix = yymm(d) + "-";

        String max;
        switch (kind == null ? "" : kind.toUpperCase()) {
            case "SALES":   max = previewRepo.findMaxSales(cc, prefix);   break;
            case "BUY":     max = previewRepo.findMaxBuy(cc, prefix);     break;
            case "MONEY":   max = previewRepo.findMaxMoney(cc, prefix);   break;
            case "PAYMENT": max = previewRepo.findMaxPayment(cc, prefix); break;
            default: throw new IllegalArgumentException("Unknown voucher kind: " + kind);
        }
        return incFromPrefix(max, prefix);
    }

    @Override
    public ReserveRes reserve(String kind, LocalDate baseDate, String userId) {
        String cc = companyCtx.getCompanyCode();
        if (cc == null || cc.isBlank()) throw new IllegalStateException("회사코드 세션 누락");

        LocalDate d = (baseDate != null ? baseDate : LocalDate.now());

        // ★ 프로시저(PR_RESERVE_VOUCHER)는 'SAL/BUY/RCV/PAY' 같은 약어를 기대
        String procType = switch (String.valueOf(kind).toUpperCase()) {
            case "SALES"   -> "SAL";
            case "BUY"     -> "BUY";
            case "MONEY"   -> "RCV"; // 수금
            case "PAYMENT" -> "PAY"; // 지급
            default -> throw new IllegalArgumentException("Unknown voucher kind: " + kind);
        };

        // repository 내부에서 PR_RESERVE_VOUCHER(p_company_code, p_voucher_type=procType, ...) 호출
        return reservationRepo.reserve(cc, procType, d, userId);
    }
}
