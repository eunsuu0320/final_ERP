package com.yedam.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.repository.PaymentRepository;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    // 결제 완료 후 COMPANY 테이블 저장
    @Transactional
    public void saveCompanyInfo(PayRequest request) {
        paymentRepository.insertCompany(request);
    }
}
