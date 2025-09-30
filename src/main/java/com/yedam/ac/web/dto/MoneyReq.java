package com.yedam.ac.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class MoneyReq {
	private String reservationId;
    private String voucherDate;   // yyyy-MM-dd (문자열로 옴)
    private String moneyCode;
    private String partnerName;
    private String employee;
    private String taxCode;       // TAXABLE/ZERO/EXEMPT
    private BigDecimal amountSupply;
    private BigDecimal amountVat;
    private BigDecimal amountTotal;
    private String remark;

    // 편의
    public LocalDate date() { return LocalDate.parse(voucherDate); }
}