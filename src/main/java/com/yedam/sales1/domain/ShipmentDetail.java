package com.yedam.sales1.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "SHIPMENT_DETAIL")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentDetail {

    @Id
    @Column(name = "SHIPMENT_DETAIL_CODE", length = 20, nullable = false)
    private String shipmentDetailCode;

    @Column(name = "SHIPMENT_CODE", length = 20, nullable = false)
    private String shipmentCode;

    @Column(name = "PRODUCT_CODE", length = 20)
    private String productCode;

    @Column(name = "QUANTITY")
    private Integer quantity;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
}
