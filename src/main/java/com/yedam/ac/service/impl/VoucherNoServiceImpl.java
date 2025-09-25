// src/main/java/com/yedam/ac/service/impl/VoucherNoServiceImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.repository.VoucherNoQueryRepository;
import com.yedam.ac.service.VoucherNoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherNoServiceImpl implements VoucherNoService {

    private final VoucherNoQueryRepository repo;
    private static final DateTimeFormatter YYMM = DateTimeFormatter.ofPattern("yyMM");

    @Override
    @Transactional(readOnly = true)
    public String next(String type, LocalDate date) {
        LocalDate base = (date != null) ? date : LocalDate.now();
        String prefix = base.format(YYMM) + "-"; // e.g. "2509-"

        String last = switch (type.toUpperCase()) {
            case "SALES" -> repo.maxSalesVoucher(prefix);
            case "BUY"   -> repo.maxBuyVoucher(prefix);
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };

        int nextNum = 1;
        if (last != null && last.startsWith(prefix)) {
            String tail = last.substring(prefix.length()); // "0012"
            try { nextNum = Integer.parseInt(tail) + 1; } catch (NumberFormatException ignore) {}
        }
        return prefix + String.format("%04d", nextNum);
    }
    
    @Override
    @Transactional(readOnly = true)
    public String next1(String type, LocalDate date) {
        LocalDate base = (date != null) ? date : LocalDate.now();
        String prefix = base.format(YYMM) + "-";

        String last = switch (type.toUpperCase()) {
            case "SALES"   -> repo.maxSalesVoucher(prefix);
            case "BUY"     -> repo.maxBuyVoucher(prefix);
            case "MONEY"   -> repo.maxMoneyVoucher(prefix);    // 추가
            case "PAYMENT" -> repo.maxPaymentVoucher(prefix);  // 추가
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };

        int nextNum = 1;
        if (last != null && last.startsWith(prefix)) {
            String tail = last.substring(prefix.length());
            try { nextNum = Integer.parseInt(tail) + 1; } catch (NumberFormatException ignore) {}
        }
        return prefix + String.format("%04d", nextNum);
    }
}
