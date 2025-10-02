package com.yedam.common.domain;

import com.yedam.common.domain.payment.PayRequest;

import lombok.Data;

@Data
public class PayRequestWrapper {
    private PayRequest payRequest;
    private Company companyInfo;
    private SystemUser systemUser;
    
    private String contactEmail;
    private String signatureDataUrl;
}

