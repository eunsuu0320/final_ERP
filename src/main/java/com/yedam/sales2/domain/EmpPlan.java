package com.yedam.sales2.domain;

import lombok.Data;

@Data
public class EmpPlan {
	private String empCode;
    private String saleCode;
    private String empName;
    private Long customerCount;
    private Long lastYearSales;
    private Double lastYearCost;
    private Long lastYearProfit;
}
