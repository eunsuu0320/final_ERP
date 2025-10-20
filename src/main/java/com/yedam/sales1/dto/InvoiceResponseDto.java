package com.yedam.sales1.dto;

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.InvoiceDetail;
import lombok.*;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDto {
    private String invoiceCode;
    private String partnerCode;
    private String partnerName;
    private String manager;
    private Date dmndDate;
    private Double dmndAmt;
    private String status;
    private List<InvoiceDetailDto> invoiceDetail;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InvoiceDetailDto {
        private String shipmentCode;           // 출하번호
        private Date shipmentDate;             // 출하일
        private Integer quantity;              // 전체수량
        private Integer totalAmount;           // 공급가액
        private Integer tax;                   // 부가세
        private Integer shipmentInvoiceAmount; // 최종금액 (합계)
    }

    public static InvoiceResponseDto from(Invoice invoice, List<InvoiceDetail> details) {
        return InvoiceResponseDto.builder()
                .invoiceCode(invoice.getInvoiceCode())
                .partnerCode(invoice.getPartnerCode())
                .partnerName(invoice.getPartnerName())
                .manager(invoice.getManager())
                .dmndDate(invoice.getDmndDate())
                .dmndAmt(invoice.getDmndAmt())
                .status(invoice.getStatus())
                .invoiceDetail(details.stream()
                        .map(d -> new InvoiceDetailDto(
                                d.getShipmentCode(),
                                d.getShipmentDate(),
                                d.getQuantity(),
                                d.getTotalAmount(),
                                d.getTax(),
                                d.getShipmentInvoiceAmount()
                        ))
                        .collect(Collectors.toList()))
                .build();
    }
}
