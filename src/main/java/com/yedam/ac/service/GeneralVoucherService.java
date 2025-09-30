package com.yedam.ac.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.repository.MoneyStatementRepository;
import com.yedam.ac.repository.PaymentStatementRepository;
import com.yedam.ac.repository.VoucherReservationRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.MoneyReq;
import com.yedam.ac.web.dto.PaymentReq;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeneralVoucherService {

    private final CompanyContext ctx;
    private final VoucherReservationRepository reservationRepo;
    private final MoneyStatementRepository moneyRepo;
    private final PaymentStatementRepository payRepo;

    private static BigDecimal nz(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }

    @Transactional
    public String saveReceipt(MoneyReq r){
        String cc = ctx.getCompanyCode();
        if (cc == null) throw new IllegalStateException("회사코드 누락");

        // 1) 예약번호 확인
        String vno = null;
        if (r.getReservationId() != null && !r.getReservationId().isBlank()) {
            vno = reservationRepo.findVoucherNoByResvId(r.getReservationId());
            if (vno == null) throw new IllegalStateException("유효하지 않은 예약(reservationId) 입니다.");
            // 2) 예약 확정
            int upd = reservationRepo.markUsed(r.getReservationId());
            if (upd == 0) throw new IllegalStateException("이미 사용되었거나 만료된 예약입니다.");
        } else {
            // 예약 없이 저장하려면 정책상 에러로 처리(권장)
            throw new IllegalStateException("예약번호가 필요합니다(reservationId).");
        }

        // 3) INSERT
        moneyRepo.insert(
            cc, vno, r.date(), r.getMoneyCode(), r.getPartnerName(),
            r.getEmployee(), r.getTaxCode(), nz(r.getAmountSupply()), nz(r.getAmountVat()),
            nz(r.getAmountTotal()), r.getRemark()
        );
        return vno;
    }

    @Transactional
    public String savePayment(PaymentReq r){
        String cc = ctx.getCompanyCode();
        if (cc == null) throw new IllegalStateException("회사코드 누락");

        String vno = null;
        if (r.getReservationId() != null && !r.getReservationId().isBlank()) {
            vno = reservationRepo.findVoucherNoByResvId(r.getReservationId());
            if (vno == null) throw new IllegalStateException("유효하지 않은 예약(reservationId) 입니다.");
            int upd = reservationRepo.markUsed(r.getReservationId());
            if (upd == 0) throw new IllegalStateException("이미 사용되었거나 만료된 예약입니다.");
        } else {
            throw new IllegalStateException("예약번호가 필요합니다(reservationId).");
        }

        payRepo.insert(
            cc, vno, r.date(), r.getBuyCode(), r.getPartnerName(),
            r.getEmployee(), r.getTaxCode(), nz(r.getAmountSupply()), nz(r.getAmountVat()),
            nz(r.getAmountTotal()), r.getRemark()
        );
        return vno;
    }
}
