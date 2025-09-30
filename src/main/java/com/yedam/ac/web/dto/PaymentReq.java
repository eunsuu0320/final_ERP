package com.yedam.ac.web.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentReq {
	private String reservationId;
    private String voucherDate;
    private String buyCode;       // front에서 BUY/PAYMENT는 buyCode로 보냄
    private String partnerName;
    private String employee;
    private String taxCode;
    private BigDecimal amountSupply;
    private BigDecimal amountVat;
    private BigDecimal amountTotal;
    private String remark;

    public LocalDate date() { return LocalDate.parse(voucherDate); }
}