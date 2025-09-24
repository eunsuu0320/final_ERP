// src/main/java/com/yedam/ac/service/impl/StatementServiceImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.domain.BuyStatement;
import com.yedam.ac.domain.SalesStatement;
import com.yedam.ac.repository.BuyStatementRepository;
import com.yedam.ac.repository.SalesStatementRepository;
import com.yedam.ac.service.StatementService;
import com.yedam.ac.util.VoucherNoGenerator;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final SalesStatementRepository salesRepo;
    private final BuyStatementRepository   buyRepo;

    @Transactional
    @Override
    public StatementCreateRes createSalesStatement(SalesCreateReq req) {
        LocalDate d = req.getVoucherDate() != null ? req.getVoucherDate() : LocalDate.now();
        String prefix = VoucherNoGenerator.monthPrefix(d);        // "yyMM-"
        String maxNo  = salesRepo.findMaxVoucherNoLike(prefix);   // 그달 최대
        String next   = VoucherNoGenerator.nextFromMax(maxNo);    // "0001"
        String voucherNo = prefix + next;                         // "2509-0001"

        SalesStatement e = new SalesStatement();
        e.setVoucherNo(voucherNo);
        e.setVoucherDate(d);
        e.setSalesCode(req.getSalesCode());
        e.setPartnerName(req.getPartnerName());
        e.setEmployee(req.getEmployee());
        e.setTaxCode(req.getTaxCode());
        e.setAmountSupply(req.getAmountSupply() == null ? 0L : req.getAmountSupply());
        e.setAmountVat   (req.getAmountVat()   == null ? 0L : req.getAmountVat());
        e.setAmountTotal (req.getAmountTotal() == null ? 0L : req.getAmountTotal());
        e.setRemark(req.getRemark());

        salesRepo.save(e);
        return new StatementCreateRes(voucherNo);
    }

    @Transactional
    @Override
    public StatementCreateRes createBuyStatement(BuyCreateReq req) {
        LocalDate d = req.getVoucherDate() != null ? req.getVoucherDate() : LocalDate.now();
        String prefix = VoucherNoGenerator.monthPrefix(d);       // "yyMM-"
        String maxNo  = buyRepo.findMaxVoucherNoLike(prefix);
        String next   = VoucherNoGenerator.nextFromMax(maxNo);   // "0001"
        String voucherNo = prefix + next;                        // "2509-0001"

        BuyStatement e = new BuyStatement();
        e.setVoucherNo(voucherNo);
        e.setVoucherDate(d);
        e.setBuyCode(req.getBuyCode());
        e.setPartnerName(req.getPartnerName());
        e.setEmployee(req.getEmployee());
        e.setTaxCode(req.getTaxCode());
        e.setAmountSupply(req.getAmountSupply() == null ? 0L : req.getAmountSupply());
        e.setAmountVat   (req.getAmountVat()   == null ? 0L : req.getAmountVat());
        e.setAmountTotal (req.getAmountTotal() == null ? 0L : req.getAmountTotal());
        e.setRemark(req.getRemark());

        buyRepo.save(e);
        return new StatementCreateRes(voucherNo);
    }
}