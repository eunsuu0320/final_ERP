// src/main/java/com/yedam/ac/service/impl/StatementServiceImpl.java
package com.yedam.ac.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.domain.BuyStatement;
import com.yedam.ac.domain.MoneyStatement;
import com.yedam.ac.domain.PaymentStatement;
import com.yedam.ac.domain.SalesStatement;
import com.yedam.ac.repository.BuyRepository;
import com.yedam.ac.repository.BuyStatementRepository;
import com.yedam.ac.repository.MoneyStatementRepository;
import com.yedam.ac.repository.PaymentStatementRepository;
import com.yedam.ac.repository.SalesLookupDao;
import com.yedam.ac.repository.SalesStatementRepository;
import com.yedam.ac.service.StatementService;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.util.VoucherNoGenerator;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.MoneyReq;
import com.yedam.ac.web.dto.PaymentReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final CompanyContext companyCtx;

    private final SalesStatementRepository salesStRepo;
    private final BuyStatementRepository buyStRepo;
    private final MoneyStatementRepository moneyStRepo;      // ▼ 추가
    private final PaymentStatementRepository paymentStRepo;  // ▼ 추가

    private final SalesLookupDao salesLookupDao;
    private final BuyRepository buyRepo;

    private final VoucherNoGenerator vnoGen;

    @Override
    @Transactional
    public StatementCreateRes createSalesStatement(SalesCreateReq req) {
        final String cc = mustCc();
        if (notBlank(req.getSalesCode())) {
            long c = salesLookupDao.countByCodeAndCompany(req.getSalesCode(), cc);
            if (c == 0) throw new IllegalArgumentException("유효하지 않은 판매코드이거나 권한 없음");
        }
        final String vno = vnoGen.nextSales(req.getVoucherDate());
        SalesStatement e = new SalesStatement();
        e.setCompanyCode(cc);
        e.setVoucherNo(vno);
        e.setVoucherDate(req.getVoucherDate());
        e.setSalesCode(req.getSalesCode());
        e.setSalesDate(req.getSalesDate());
        e.setPartnerCode(req.getPartnerCode());
        e.setPartnerName(req.getPartnerName());
        e.setEmployee(req.getEmployee());
        e.setProductCode(req.getProductCode());
        e.setUnitPrice(req.getUnitPrice());
        e.setTaxCode(req.getTaxCode());
        e.setAmountSupply(req.getAmountSupply());
        e.setAmountVat(req.getAmountVat());
        e.setAmountTotal(req.getAmountTotal());
        e.setRemark(req.getRemark());
        salesStRepo.save(e);
        return new StatementCreateRes(vno);
    }

    @Override
    @Transactional
    public StatementCreateRes createBuyStatement(BuyCreateReq req) {
        final String cc = mustCc();
        if (notBlank(req.getBuyCode())) {
            long c = buyRepo.countByCodeAndCompany(req.getBuyCode(), cc);
            if (c == 0) throw new IllegalArgumentException("유효하지 않은 매입코드이거나 권한 없음");
        }
        final String vno = vnoGen.nextBuy(req.getVoucherDate());
        BuyStatement e = new BuyStatement();
        e.setCompanyCode(cc);
        e.setVoucherNo(vno);
        e.setVoucherDate(req.getVoucherDate());
        e.setBuyCode(req.getBuyCode());
        e.setBuyDate(req.getBuyDate());
        e.setPartnerCode(req.getPartnerCode());
        e.setPartnerName(req.getPartnerName());
        e.setEmployee(req.getEmployee());
        e.setProductCode(req.getProductCode());
        e.setUnitPrice(req.getUnitPrice());
        e.setTaxCode(req.getTaxCode());
        e.setAmountSupply(req.getAmountSupply());
        e.setAmountVat(req.getAmountVat());
        e.setAmountTotal(req.getAmountTotal());
        e.setRemark(req.getRemark());
        buyStRepo.save(e);
        return new StatementCreateRes(vno);
    }

    // ▼▼ 수금
    @Override
    @Transactional
    public StatementCreateRes createMoneyStatement(MoneyReq req) {
        final String cc = mustCc();

        // vnoGen에 nextMoney(LocalDate)가 있다고 가정 (매출/매입과 동일한 방식)
        final String vno = vnoGen.nextMoney(req.date());

        MoneyStatement e = new MoneyStatement();
        e.setCompanyCode(cc);
        e.setVoucherNo(vno);
        e.setMoneyDate(req.date());
        // moneyCode가 없으면 invoiceCode로 대체
        e.setMoneyCode(notBlank(req.getMoneyCode()) ? req.getMoneyCode() : req.getInvoiceCode());
        e.setPartnerName(req.getPartnerName());
        e.setEmployee(req.getEmployee());
        e.setTaxCode(req.getTaxCode());
        e.setAmountSupply(req.getAmountSupply());
        e.setAmountVat(req.getAmountVat());
        e.setAmountTotal(req.getAmountTotal());
        e.setRemark(req.getRemark());

        moneyStRepo.save(e);
        return new StatementCreateRes(vno);
    }

    // ▼▼ 지급
    @Override
    @Transactional
    public StatementCreateRes createPaymentStatement(PaymentReq req) {
        final String cc = mustCc();

        // vnoGen에 nextPayment(LocalDate)가 있다고 가정
        final String vno = vnoGen.nextPayment(req.date());

        PaymentStatement e = new PaymentStatement();
        e.setCompanyCode(cc);
        e.setVoucherNo(vno);
        e.setPaymentDate(req.date());
        // 프론트 DTO가 buyCode를 보내므로 paymentCode로 그대로 세팅
        e.setPaymentCode(req.getBuyCode());
        e.setPartnerName(req.getPartnerName());
        e.setEmployee(req.getEmployee());
        e.setTaxCode(req.getTaxCode());
        e.setAmountSupply(req.getAmountSupply());
        e.setAmountVat(req.getAmountVat());
        e.setAmountTotal(req.getAmountTotal());
        e.setRemark(req.getRemark());

        paymentStRepo.save(e);
        return new StatementCreateRes(vno);
    }

    private String mustCc() {
        String cc = companyCtx.getCompanyCode();
        if (cc == null || cc.isBlank()) throw new IllegalStateException("회사코드 세션 누락");
        return cc;
    }
    private static boolean notBlank(String s){ return s!=null && !s.isBlank(); }
}
