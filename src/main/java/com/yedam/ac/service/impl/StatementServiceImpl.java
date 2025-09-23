package com.yedam.ac.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.ac.domain.BuyStatement;
import com.yedam.ac.domain.SalesStatement;
import com.yedam.ac.domain.Statement;
import com.yedam.ac.repository.BuyStatementRepository;
import com.yedam.ac.repository.CommonQueryRepository;
import com.yedam.ac.repository.SalesStatementRepository;
import com.yedam.ac.repository.StatementRepository;
import com.yedam.ac.service.StatementService;
import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Builder
@Service
@RequiredArgsConstructor
public class StatementServiceImpl implements StatementService {

    private final StatementRepository statementRepo;
    private final SalesStatementRepository salesRepo;
    private final BuyStatementRepository buyRepo;
    private final CommonQueryRepository commonQuery;

    @Override
    @Transactional
    public StatementCreateRes createSalesStatement(SalesCreateReq req) {
        String voucherNo = commonQuery.nextVoucherNo();

        statementRepo.save(Statement.builder()
                .voucherNo(voucherNo)
                .companyCode("C001")
                .voucherTypeCode("SALES")
                .voucherStatusCode("NORMAL")
                .build());

        salesRepo.save(SalesStatement.builder()
                .voucherNo(voucherNo)
                .voucherDate(req.getVoucherDate())
                .salesCode(req.getSalesCode())
                .partnerName(req.getPartnerName())
                .employee(req.getEmployee())
                .taxCode(req.getTaxCode())
                .amountSupply(req.getAmountSupply())
                .amountVat(req.getAmountVat())
                .amountTotal(req.getAmountTotal())
                .remark(req.getRemark())
                .build());

        return new StatementCreateRes(voucherNo);
    }

    @Override
    @Transactional
    public StatementCreateRes createBuyStatement(BuyCreateReq req) {
        String voucherNo = commonQuery.nextVoucherNo();

        statementRepo.save(Statement.builder()
                .voucherNo(voucherNo)
                .companyCode("C001")
                .voucherTypeCode("BUY")
                .voucherStatusCode("NORMAL")
                .build());

        buyRepo.save(BuyStatement.builder()
                .voucherNo(voucherNo)
                .voucherDate(req.getVoucherDate())
                .buyCode(req.getBuyCode())
                .partnerName(req.getPartnerName())
                .employee(req.getEmployee())
                .taxCode(req.getTaxCode())
                .amountSupply(req.getAmountSupply())
                .amountVat(req.getAmountVat())
                .amountTotal(req.getAmountTotal())
                .remark(req.getRemark())
                .build());

        return new StatementCreateRes(voucherNo);
    }
}
