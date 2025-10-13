package com.yedam.sales1.domain;

import jakarta.persistence.*; // jakarta.persistence.* 임포트 유지
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Entity
@Table(name = "ORDERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Sequence Generator 정의 (DB 시퀀스 이름은 'ORDER_SEQ'라고 가정)
@SequenceGenerator(
    name = "ORDER_SEQ_GENERATOR", // JPA에서 사용할 Generator 이름
    sequenceName = "ORDER_SEQ",   // ⭐⭐ 실제 DB에 존재하는 시퀀스 이름으로 변경해야 합니다.
    initialValue = 1,             // 시퀀스 시작 값 (DB에서 설정하는 것이 일반적)
    allocationSize = 1            // 시퀀스 증가 값
)
public class Orders {

    @Id
    @GeneratedValue(
        strategy = GenerationType.SEQUENCE, // Sequence 전략 사용 지정
        generator = "ORDER_SEQ_GENERATOR" // 위에서 정의한 Generator 이름 지정
    )
    @Column(name = "ORDER_UNIQUE_CODE", nullable = false)
    private Long orderUniqueCode; // ⭐⭐⭐ Sequence에 의해 자동 생성됩니다.

    @Column(name = "ESTIMATE_UNIQUE_CODE", nullable = false)
    private Long estimateUniqueCode;

    @Column(name = "ORDER_CODE", length = 20, nullable = false)
    private String orderCode; // ORD0001 형식의 코드는 서비스에서 생성 후 할당됩니다.

    @Column(name = "PARTNER_CODE", length = 20, nullable = false)
    private String partnerCode;

    @Column(name = "CREATE_DATE", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date createDate;

    @Column(name = "TOTAL_AMOUNT")
    private Double totalAmount;

    @Column(name = "DELIVERY_DATE")
    @Temporal(TemporalType.DATE)
    private Date deliveryDate;

    @Column(name = "MANAGER", length = 20, nullable = false)
    private String manager;

    @Column(name = "STATUS", length = 20)
    private String status;

    @Column(name = "VERSION", nullable = false)
    private Integer version;

    @Column(name = "IS_CURRENT_VERSION", length = 10, nullable = false)
    private String isCurrentVersion;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;

    @PrePersist
    public void prePersist() {
        Date now = new Date();
        if (createDate == null) createDate = now;
        if (version == null) version = 1;
        if (isCurrentVersion == null) isCurrentVersion = "Y";
    }
}