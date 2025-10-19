package com.yedam.sales1.domain;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "SHIPMENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @Column(name = "SHIPMENT_CODE", length = 20, nullable = false)
    private String shipmentCode;

    @Column(name = "SHIPMENT_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date shipmentDate;

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "WAREHOUSE", length = 30)
    private String warehouse;

    @Column(name = "TOTAL_QUANTITY")
    private Integer totalQuantity;

    @Column(name = "CREATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "UPDATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date updateDate;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;

    @Column(name = "POST_CODE")
    private Integer postCode;

    @Column(name = "NOW_QUANTITY")
    private Integer nowQuantity;

    @Column(name = "ADDRESS", length = 1000)
    private String address;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
}
