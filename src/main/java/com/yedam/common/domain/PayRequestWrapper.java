package com.yedam.common.domain;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.payment.PayRequest;
import lombok.Data;

@Data
public class PayRequestWrapper {
    private PayRequest payRequest;
    private Company companyInfo;
}

