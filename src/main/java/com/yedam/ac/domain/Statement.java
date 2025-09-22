package com.yedam.ac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "STATEMENT")
public class Statement {
    @Id
    @Column(name = "VOUCHER_NO")
    private Long voucherNo;

    @Column(name = "COMPANY_CODE")
    private String companyCode;
}
