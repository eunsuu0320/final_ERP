package com.yedam.ac.domain;

import java.time.LocalDate;

import com.yedam.ac.domain.Statement.StatementBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "BUY_STATEMENT")
@SequenceGenerator(name="buy_stmt_seq", sequenceName="BUY_STATEMENT_SEQ", allocationSize=1)
@Data
public class BuyStatement {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buy_stmt_seq")
    @Column(name = "VOUCHER_NO")
    private Long voucherNo;

    @Column(name = "VOUCHER_DATE")
    private LocalDate voucherDate;

    @Column(name = "BUY_CODE")             // ★ 매입은 BUY_CODE
    private String buyCode;

    @Column(name = "PARTNER_NAME")
    private String partnerName;

    @Column(name = "EMPLOYEE")
    private String employee;

    @Column(name = "TAX_CODE")
    private String taxCode;

    @Column(name = "AMOUNT_SUPPLY")
    private Long amountSupply;

    @Column(name = "AMOUNT_VAT")
    private Long amountVat;

    @Column(name = "AMOUNT_TOTAL")
    private Long amountTotal;

    @Column(name = "REMARK")
    private String remark;

	public static StatementBuilder builder() {
		// TODO Auto-generated method stub
		return null;
	}
}
