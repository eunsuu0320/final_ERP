package com.yedam.ac.web.dto;

import java.time.LocalDate;

import com.yedam.ac.domain.AcInvoice;

import lombok.Data;

@Data
public class AcInvoiceModalRow {
    private String invoiceUniqueCode;
    private String invoiceCode;
    private String partnerCode;
    private String partnerName; // 필요 시 조인해 세팅
    private LocalDate createDate;
    private LocalDate dmndDate;
    private LocalDate rcptDate;
    private Long dmndAmt;
    private String status;
    private String companyCode;

    public static AcInvoiceModalRow from(AcInvoice e){
        AcInvoiceModalRow d = new AcInvoiceModalRow();
        d.invoiceUniqueCode = e.getInvoiceUniqueCode();
        d.invoiceCode = e.getInvoiceCode();
        d.partnerCode = e.getPartnerCode();
        d.partnerName = e.getPartnerName();
        d.createDate = e.getCreateDate();
        d.dmndDate = e.getDmndDate();
        d.rcptDate = e.getRecptDate();
        d.dmndAmt = e.getDmndAmt();
        d.status = e.getStatus();
        d.companyCode = e.getCompanyCode();
        return d;
    }
}
