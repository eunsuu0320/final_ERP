// src/main/java/com/yedam/ac/service/impl/StatementServiceImpl.java
package com.yedam.ac.service.impl;

import java.time.LocalDate;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.domain.BuyStatement;
import com.yedam.ac.domain.SalesStatement;
import com.yedam.ac.repository.BuyStatementRepository;
import com.yedam.ac.repository.SalesStatementRepository;
import com.yedam.ac.service.StatementService;
import com.yedam.ac.service.VoucherNoService;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final SalesStatementRepository salesRepo;
    private final BuyStatementRepository buyRepo;
    private final VoucherNoService vnoSvc;

    @Override
    @Transactional
    public StatementCreateRes createSalesStatement(SalesCreateReq req) {
        LocalDate d = req.getVoucherDate() != null ? req.getVoucherDate() : LocalDate.now();

        int tries = 0;
        while (true) {
            String vno = vnoSvc.next("SALES", d);
            try {
                SalesStatement e = new SalesStatement();
                e.setVoucherNo(vno);
                e.setVoucherDate(d);
                e.setSalesCode(req.getSalesCode());
                e.setPartnerName(req.getPartnerName());
                e.setEmployee(req.getEmployee());
                e.setTaxCode(req.getTaxCode());
                e.setAmountSupply(req.getAmountSupply());
                e.setAmountVat(req.getAmountVat());
                e.setAmountTotal(req.getAmountTotal());
                e.setRemark(req.getRemark());

                SalesStatement saved = salesRepo.save(e);
                return new StatementCreateRes(saved.getVoucherNo());
            } catch (DataIntegrityViolationException ex) {
                if (++tries >= 3) throw ex; // 3회 재시도 후 포기
            }
        }
    }

    @Override
    @Transactional
    public StatementCreateRes createBuyStatement(BuyCreateReq req) {
        LocalDate d = req.getVoucherDate() != null ? req.getVoucherDate() : LocalDate.now();

        int tries = 0;
        while (true) {
            String vno = vnoSvc.next("BUY", d);
            try {
                BuyStatement e = new BuyStatement();
                e.setVoucherNo(vno);
                e.setVoucherDate(d);
                e.setBuyCode(req.getBuyCode());
                e.setPartnerName(req.getPartnerName());
                e.setEmployee(req.getEmployee());
                e.setTaxCode(req.getTaxCode());
                e.setAmountSupply(req.getAmountSupply());
                e.setAmountVat(req.getAmountVat());
                e.setAmountTotal(req.getAmountTotal());
                e.setRemark(req.getRemark());

                BuyStatement saved = buyRepo.save(e);
                return new StatementCreateRes(saved.getVoucherNo());
            } catch (DataIntegrityViolationException ex) {
                if (++tries >= 3) throw ex;
            }
        }
    }
}
