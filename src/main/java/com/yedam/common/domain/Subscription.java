package com.yedam.common.domain;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Subscription implements Prefixable {

    @Id
    @GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
    private String subscriptionCode;  		 // 구독코드

    private String companyCode;       		 // 회사코드

    private Date subscriptionStartDate; 	 // 구독 시작일
    private Date subscriptionEndDate;   	 // 구독 만료일
    private String periodMonth;              // 구독 기간
    private Long price;                      // 결제 금액
    private String status;                   // 구독 상태
    private String remk;                     // 비고

    @Override
    public String getPrefix() { return "SUB"; }

    @Override
    public String getSequenceName() { return "SUBSCRIPTION_SEQ"; }
}
