package com.yedam.sales1.domain; // 실제 패키지명에 맞게 수정하세요

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
@Data // @Getter, @Setter, @ToString, @EqualsAndHashCode 등을 포함
@NoArgsConstructor // JPA 사용 및 @Data와의 호환성을 위한 기본 생성자
@AllArgsConstructor // @Builder 사용을 위한 모든 필드를 포함하는 생성자
@Builder // 빌더 패턴 사용
public class ShipmentDetail {

    @Id // 기본 키 지정
    @Column(name = "SHIPMENT_DETAIL_CODE", length = 20, nullable = false)
    private String shipmentDetailCode; // SHIPMENT_DETAIL_CODE (VARCHAR2(20) NOT NULL)

    @Column(name = "SHIPMENT_CODE", length = 20, nullable = false)
    private String shipmentCode; // SHIPMENT_CODE (VARCHAR2(20) NOT NULL)

    @Column(name = "PRODUCT_CODE", length = 20, nullable = true) // NULL 허용
    private String productCode; // PRODUCT_CODE (VARCHAR2(20) NULL)

    @Column(name = "QUANTITY", nullable = true) // NULL 허용
    private Integer quantity; // QUANTITY (NUMBER NULL)

    @Column(name = "REMARKS", length = 1000, nullable = true) // NULL 허용
    private String remarks; // REMARKS (VARCHAR2(1000) NULL)

    @Column(name = "STATUS", length = 20, nullable = true) // NULL 허용
    private String status; // STATUS (VARCHAR2(20) NULL)

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode; // COMPANY_CODE (VARCHAR2(20) NOT NULL)
}