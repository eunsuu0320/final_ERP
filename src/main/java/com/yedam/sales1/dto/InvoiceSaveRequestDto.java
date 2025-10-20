package com.yedam.sales1.dto;

import com.yedam.sales1.domain.Invoice;
import lombok.*;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceSaveRequestDto {
    private String partnerCode;
    private String partnerName;
    private String manager;
    private Date dmndDate;
    private Double dmndAmt;
    private String status;
    private String companyCode;
    private Integer unrctBaln;
    private List<InvoiceDetailDto> invoiceDetail;

    // ✅ 디테일 DTO - 수정됨
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class InvoiceDetailDto {
        private String shipmentCode;           // 출하번호
        private Date shipmentDate;             // 출하일
        private Integer quantity;              // 전체수량
        private Integer totalAmount;           // 공급가액
        private Integer tax;                   // 부가세
        private Integer shipmentInvoiceAmount; // 최종금액 (합계)
    }

    // ✅ 엔티티 변환 메소드
    public Invoice toEntity(String invoiceCode) {
        return Invoice.builder()
                .invoiceCode(invoiceCode)
                .partnerCode(partnerCode)
                .partnerName(partnerName)
                .manager(manager)
                .dmndDate(dmndDate)
                .dmndAmt(dmndAmt)
                .status(status)
                .companyCode(companyCode)
                .unrctBaln(unrctBaln)
                .build();
    }
}
