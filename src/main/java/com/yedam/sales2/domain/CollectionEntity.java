package com.yedam.sales2.domain;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;
import com.yedam.sales1.domain.Partner;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "Collection")
@Table(name = "COLLECTION")
public class CollectionEntity implements Prefixable{

	@Id
	@GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
	@Column(name = "MONEY_CODE")
    private String moneyCode; // 수금코드

    @Column(name = "MONEY_DATE")
    private Date moneyDate;   // 수금일자

    @Column(name = "RECPT")
    private Long recpt;       // 수금금액

    @Column(name = "PAYMENT_METHODS")
    private String paymentMethods; // 결제방식
    
	@Column(name = "POST_DEDUCTION")
	private Long postDeduction; // 사후공제
    
    @Column(name = "REMK")
    private String remk; // 비고

    // 연관만 사용 (외래키 저장/조회 포함해서 이 연관으로 처리)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "PARTNER_CODE",  referencedColumnName = "PARTNER_CODE"),
        @JoinColumn(name = "COMPANY_CODE", referencedColumnName = "COMPANY_CODE")
    })
    private Partner partner;

    @Override
    public String getPrefix() {
        return "M";
    }

    @Override
    public String getSequenceName() {
        return "COLLECTION_SEQ";
    }

    // 편의 접근자(필요시)
    public String getPartnerCode() { return partner != null ? partner.getPartnerCode() : null; }
    public String getCompanyCode() { return partner != null ? partner.getCompanyCode() : null; }
}