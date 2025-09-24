// com.yedam.ac.repository.CommonQueryRepositoryImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.repository.CommonQueryRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CommonQueryRepositoryImpl implements CommonQueryRepository {
    private final EntityManager em;

    @Override
    @Transactional
    public String nextVoucherNo(String voucherType, LocalDate voucherDate) {
        if (voucherDate == null) voucherDate = LocalDate.now();
        String yymm = String.format("%02d%02d", voucherDate.getYear() % 100, voucherDate.getMonthValue()); // "2509"

        Integer cur = null;
        try {
            cur = ((Number) em.createNativeQuery(
                "SELECT LAST_SEQ FROM VOUCHER_SERIAL WHERE VOUCHER_TYPE_CODE = :t AND YYMM = :m FOR UPDATE")
                .setParameter("t", voucherType)
                .setParameter("m", yymm)
                .getSingleResult()).intValue();
        } catch (NoResultException ignored) {}

        int next;
        if (cur == null) {
            em.createNativeQuery(
                "INSERT INTO VOUCHER_SERIAL (VOUCHER_TYPE_CODE, YYMM, LAST_SEQ) VALUES (:t, :m, 1)")
                .setParameter("t", voucherType)
                .setParameter("m", yymm)
                .executeUpdate();
            next = 1;
        } else {
            next = cur + 1;
            em.createNativeQuery(
                "UPDATE VOUCHER_SERIAL SET LAST_SEQ = :n WHERE VOUCHER_TYPE_CODE = :t AND YYMM = :m")
                .setParameter("n", next)
                .setParameter("t", voucherType)
                .setParameter("m", yymm)
                .executeUpdate();
        }

        return yymm + "-" + String.format("%04d", next);
    }
}
