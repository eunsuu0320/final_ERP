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

	@Column(name = "LOAN_INVOICE_AMOUNT", nullable = false)
	private Integer loanInvoiceAmount;
	
	@Column(name = "LOAN_INVOICE_REASON", length = 1000, nullable = false)
	private String loanInvoiceReason;
	
	@Column(name = "COMPANY_CODE", length = 20, nullable = false)
	private String companyCode;

}
