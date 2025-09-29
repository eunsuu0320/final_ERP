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
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final SalesStatementRepository salesRepo;
    private final BuyStatementRepository   buyRepo;
    private final VoucherNoService         vnoSvc;
    private final CompanyContext           companyCtx;

    @Override
    @Transactional
    public StatementCreateRes createSalesStatement(SalesCreateReq req) {
        final String companyCode = companyCtx.getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalStateException("회사코드 누락으로 매출전표를 등록할 수 없습니다.");
        }

        LocalDate d = (req.getVoucherDate() != null) ? req.getVoucherDate() : LocalDate.now();
        int tries = 0;
        while (true) {
            String vno = vnoSvc.next("SALES", d, companyCode);
            try {
                SalesStatement e = new SalesStatement();
                e.setCompanyCode(companyCode);
                e.setVoucherNo(vno);
                e.setVoucherDate(d);

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

                SalesStatement saved = salesRepo.save(e);
                return new StatementCreateRes(saved.getVoucherNo());
            } catch (DataIntegrityViolationException ex) {
                if (++tries >= 3) throw ex;
            }
        }
    }

    @Override
    @Transactional
    public StatementCreateRes createBuyStatement(BuyCreateReq req) {
        final String companyCode = companyCtx.getCompanyCode();
        if (companyCode == null || companyCode.isBlank()) {
            throw new IllegalStateException("회사코드 누락으로 매입전표를 등록할 수 없습니다.");
        }

        LocalDate d = (req.getVoucherDate() != null) ? req.getVoucherDate() : LocalDate.now();
        int tries = 0;
        while (true) {
            String vno = vnoSvc.next("BUY", d, companyCode);
            try {
                BuyStatement e = new BuyStatement();
                e.setCompanyCode(companyCode);
                e.setVoucherNo(vno);
                e.setVoucherDate(d);

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

                BuyStatement saved = buyRepo.save(e);
                return new StatementCreateRes(saved.getVoucherNo());
            } catch (DataIntegrityViolationException ex) {
                if (++tries >= 3) throw ex;
            }
        }
    }
}
