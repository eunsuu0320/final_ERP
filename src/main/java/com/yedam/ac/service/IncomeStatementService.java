// src/main/java/com/yedam/ac/service/IncomeStatementService.java
package com.yedam.ac.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.yedam.ac.repository.IncomeStatementRepository;
import com.yedam.ac.util.CompanyContext;
import com.yedam.ac.web.dto.IncomeStatementDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class IncomeStatementService {
  private final CompanyContext companyCtx;
  private final IncomeStatementRepository repo;

  public IncomeStatementDto get(LocalDate from, LocalDate to){
    String cc = companyCtx.getCompanyCode();
    if (cc == null || cc.isBlank()) throw new IllegalStateException("회사코드 세션 누락");

    BigDecimal sales = nz(repo.sumSalesTotal(cc, from, to));    // 상품매출 = 매출
    BigDecimal cogs  = nz(repo.sumBuyTotal(cc, from, to));      // 제품매출원가 = 매출원가 (매입총액)
    BigDecimal gp    = sales.subtract(cogs);                    // 매출총이익

    BigDecimal salary = nz(repo.sumSalary(cc, from, to));       // 직원급여
    BigDecimal sga    = salary;                                 // 판관비 = 급여
    BigDecimal net    = gp.subtract(sga);                       // 당기순이익 = 총이익 - 판관비

    return IncomeStatementDto.builder()
        .sales(sales)
        .productSales(sales)
        .cogs(cogs.negate())        // 표에서 원가/비용을 음수로 보려면 negate(), 양수로 보려면 제거
        .productCogs(cogs.negate())
        .grossProfit(gp)
        .salary(salary)
        .sga(sga)
        .netIncome(net)
        .build();
  }

  private static BigDecimal nz(BigDecimal v){ return v==null? BigDecimal.ZERO : v; }
}
