package com.yedam.ac.web.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class BuyCreateReq {
    private LocalDate voucherDate;
    private String buyCode;
    private LocalDate buyDate;

    private String partnerCode;
    private String partnerName;

    private String employee;
    private String productCode;
    private Long unitPrice;

    private String taxCode;
    private Long amountSupply;
    private Long amountVat;
    private Long amountTotal;

    private String remark;
}
