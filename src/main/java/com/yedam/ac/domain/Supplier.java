package com.yedam.ac.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SUPPLIERS")
public class Supplier {

    @Id
    @Column(name = "SUPPLIER_ID")
    private Long supplierId; // 시퀀스+트리거가 넣어줌

    @Column(name = "SUPPLIER_CODE", nullable = false, unique = true, length = 30)
    private String supplierCode;

    @Column(name = "SUPPLIER_NAME", nullable = false, length = 200)
    private String supplierName;

    @Column(name = "PHONE", length = 30)
    private String phone;

    @Column(name = "ADDRESS", length = 300)
    private String address;

    @Column(name = "REMARK", length = 4000)
    private String remark;

    protected Supplier() {}

    // --- getters ---
    public Long getSupplierId() { return supplierId; }
    public String getSupplierCode() { return supplierCode; }
    public String getSupplierName() { return supplierName; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getRemark() { return remark; }
}
