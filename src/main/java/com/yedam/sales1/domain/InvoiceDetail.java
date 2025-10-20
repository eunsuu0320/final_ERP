package com.yedam.sales1.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Date;

@Entity
@Table(name = "INVOICE_DETAIL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceDetail {

    @Id
    @Column(name = "INVOICE_DETAIL_CODE", length = 20, nullable = false)
    private String invoiceDetailCode;

    @Column(name = "INVOICE_UNIQUE_CODE", nullable = false)
    private Integer invoiceUniqueCode;

    @Column(name = "SHIPMENT_CODE", length = 20, nullable = false)
    private String shipmentCode;

    @Column(name = "SHIPMENT_INVOICE_AMOUNT", nullable = false)
    private Integer shipmentInvoiceAmount;

    // ✅ 새로 추가된 컬럼들
    @Column(name = "TOTAL_AMOUNT")
    private Integer totalAmount; // 공급가액 (총액)

    @Column(name = "TAX")
    private Integer tax; // 부가세

    @Column(name = "QUANTITY")
    private Integer quantity; // 전체수량

    @Temporal(TemporalType.DATE)
    @Column(name = "SHIPMENT_DATE")
    private Date shipmentDate; // 출하일

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
}
