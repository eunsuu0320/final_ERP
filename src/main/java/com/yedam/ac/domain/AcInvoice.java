package com.yedam.ac.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** INVOICE(청구서) */
@Entity
@Table(name = "INVOICE")
public class AcInvoice {

    @Id
    @Column(name = "INVOICE_UNIQUE_CODE", length = 20, nullable = false)
    private String invoiceUniqueCode;

    @Column(name = "INVOICE_CODE", length = 20, nullable = false)
    private String invoiceCode;

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;
    
    @Column(name = "PARTNER_NAME", length = 20, nullable = false)
    private String partnerName;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;

    @Column(name = "CREATE_DATE", nullable = false)
    private LocalDate createDate;

    @Column(name = "DMND_DATE")
    private LocalDate dmndDate;   // 청구일

    @Column(name = "RECPT_DATE")
    private LocalDate recptDate;  // 수금(입금)예정일

    @Column(name = "DMND_AMT")
    private Long dmndAmt;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "IS_CURRENT_VERSION", length = 10, nullable = false)
    private String isCurrentVersion;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;

    // === getters / setters ===
    public String getInvoiceUniqueCode() { return invoiceUniqueCode; }
    public void setInvoiceUniqueCode(String v) { this.invoiceUniqueCode = v; }

    public String getInvoiceCode() { return invoiceCode; }
    public void setInvoiceCode(String v) { this.invoiceCode = v; }

    public String getPartnerCode() { return partnerCode; }
    public void setPartnerCode(String v) { this.partnerCode = v; }
    
    public String getPartnerName() { return partnerName; }
    public void setPartnerName(String v) { this.partnerName = v; }

    public String getManager() { return manager; }
    public void setManager(String v) { this.manager = v; }

    public LocalDate getCreateDate() { return createDate; }
    public void setCreateDate(LocalDate v) { this.createDate = v; }

    public LocalDate getDmndDate() { return dmndDate; }
    public void setDmndDate(LocalDate v) { this.dmndDate = v; }

    public LocalDate getRecptDate() { return recptDate; }
    public void setRecptDate(LocalDate v) { this.recptDate = v; }

    public Long getDmndAmt() { return dmndAmt; }
    public void setDmndAmt(Long v) { this.dmndAmt = v; }

    public String getStatus() { return status; }
    public void setStatus(String v) { this.status = v; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer v) { this.version = v; }

    public String getIsCurrentVersion() { return isCurrentVersion; }
    public void setIsCurrentVersion(String v) { this.isCurrentVersion = v; }

    public String getCompanyCode() { return companyCode; }
    public void setCompanyCode(String v) { this.companyCode = v; }
}
