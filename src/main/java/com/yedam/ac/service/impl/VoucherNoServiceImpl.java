// src/main/java/com/yedam/ac/service/impl/VoucherNoServiceImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.yedam.ac.repository.VoucherNoQueryRepository;
import com.yedam.ac.service.VoucherNoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VoucherNoServiceImpl implements VoucherNoService {

    private final VoucherNoQueryRepository repo;

    @Override
    public String next(String type, LocalDate date, String companyCode) {
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalStateException("회사코드 누락으로 전표번호를 생성할 수 없습니다.");
        }

        LocalDate d = (date != null) ? date : LocalDate.now();
        // yymm- 형식
        String prefix = String.format("%ty%<tm-", d);

        String max = repo.findMaxSequence(companyCode, prefix); // e.g. "2509-0053"
        int next = 1;
        if (max != null && max.length() >= 4) {
            try {
                next = Integer.parseInt(max.substring(max.length() - 4)) + 1;
            } catch (NumberFormatException ignore) {}
        }
        return prefix + String.format("%04d", next);
    }
}
