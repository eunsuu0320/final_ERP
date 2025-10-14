// src/main/java/com/yedam/ac/web/dto/IncomeStatementDto.java
package com.yedam.ac.web.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IncomeStatementDto {
  // 표시용 항목
  private BigDecimal sales;           // 1. 매출 = 상품매출
  private BigDecimal productSales;    //   - 상품매출(동일 값)

  private BigDecimal cogs;            // 2. 매출원가 = 제품매출원가
  private BigDecimal productCogs;     //   - 제품매출원가(동일 값)

  private BigDecimal grossProfit;     // 3. 매출 총 이익

  private BigDecimal sga;             // 4. 판관비 및 일반관리비 = 직원 급여
  private BigDecimal salary;          //   - 직원 급여

  private BigDecimal netIncome;       // 5. 당기순이익
}
