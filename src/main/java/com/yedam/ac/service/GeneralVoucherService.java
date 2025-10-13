// GeneralVoucherService.java
package com.yedam.ac.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.domain.MoneyStatement;
import com.yedam.ac.domain.PaymentStatement;
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
    private final MoneyStatementRepository moneyRepo;      // JPA
    private final PaymentStatementRepository payRepo;      // JPA

    private static BigDecimal nz(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }

    @Transactional
    public String saveReceipt(MoneyReq r){
        String cc = ctx.getCompanyCode();
        if (cc == null) throw new IllegalStateException("회사코드 누락");

        // 1) 예약번호 확인 & 확정
        String vno = resolveVoucherNoOrThrow(r.getReservationId());

        // 2) 엔티티 생성 후 저장
        MoneyStatement e = new MoneyStatement();
        e.setCompanyCode(cc);
        e.setVoucherNo(vno);
//        e.setVoucherDate(r.date());             // 필요시
        e.setMoneyDate(r.date());
        e.setMoneyCode( (r.getMoneyCode()!=null && !r.getMoneyCode().isBlank())
                        ? r.getMoneyCode() : r.getInvoiceCode());
        e.setPartnerName(r.getPartnerName());
        e.setEmployee(r.getEmployee());
        e.setTaxCode(r.getTaxCode());
        e.setAmountSupply(nz(r.getAmountSupply()));
        e.setAmountVat(nz(r.getAmountVat()));
        e.setAmountTotal(nz(r.getAmountTotal()));
        e.setRemark(r.getRemark());

        moneyRepo.save(e);
        return vno;
    }

    @Transactional
    public String savePayment(PaymentReq r){
        String cc = ctx.getCompanyCode();
        if (cc == null) throw new IllegalStateException("회사코드 누락");

        String vno = resolveVoucherNoOrThrow(r.getReservationId());

        PaymentStatement e = new PaymentStatement();
        e.setCompanyCode(cc);
        e.setVoucherNo(vno);
//        e.setVoucherDate(r.date());             // 필요시
        e.setPaymentDate(r.date());
        e.setPaymentCode(r.getBuyCode());       // 프론트에서 buyCode로 전달됨
        e.setPartnerName(r.getPartnerName());
        e.setEmployee(r.getEmployee());
        e.setTaxCode(r.getTaxCode());
        e.setAmountSupply(nz(r.getAmountSupply()));
        e.setAmountVat(nz(r.getAmountVat()));
        e.setAmountTotal(nz(r.getAmountTotal()));
        e.setRemark(r.getRemark());

        payRepo.save(e);
        return vno;
    }

    private String resolveVoucherNoOrThrow(String reservationId){
        if (reservationId == null || reservationId.isBlank())
            throw new IllegalStateException("예약번호가 필요합니다(reservationId).");

        String vno = reservationRepo.findVoucherNoByResvId(reservationId);
        if (vno == null) throw new IllegalStateException("유효하지 않은 예약(reservationId) 입니다.");

        int upd = reservationRepo.markUsed(reservationId);
        if (upd == 0) throw new IllegalStateException("이미 사용되었거나 만료된 예약입니다.");

        return vno;
    }
}
