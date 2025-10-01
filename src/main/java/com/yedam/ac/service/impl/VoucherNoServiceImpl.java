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
        if (maxVoucherNoOrNull == null || maxVoucherNoOrNull.isBlank()) {
            return prefix + "0001";
        }
        int pos = maxVoucherNoOrNull.indexOf('-');
        String seqPart = (pos >= 0) ? maxVoucherNoOrNull.substring(pos + 1) : "0000";
        int seq = 0;
        try { seq = Integer.parseInt(seqPart); } catch (Exception ignore) {}
        return prefix + String.format("%04d", seq + 1);
    }

    /** 프리뷰: DB 변경 없음(번호 상승 X). 종류별로 분리된 테이블의 MAX를 기준으로 계산 */
    @Override
    public String previewNext(String kind, LocalDate baseDate) {
        String cc = companyCtx.getCompanyCode();
        if (cc == null || cc.isBlank())
            throw new IllegalStateException("회사코드 세션 누락");

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

    /** 예약(확정 발번): 저장 직전에만 호출 — DB에서 경합/중복 안전하게 보장 */
    @Override
    public ReserveRes reserve(String kind, LocalDate baseDate, String userId) {
        String cc = companyCtx.getCompanyCode();
        if (cc == null || cc.isBlank())
            throw new IllegalStateException("회사코드 세션 누락");

        LocalDate d = (baseDate != null ? baseDate : LocalDate.now());
        return reservationRepo.reserve(cc, kind, d, userId);
    }
}
