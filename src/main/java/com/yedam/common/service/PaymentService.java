package com.yedam.common.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.common.domain.Company;
import com.yedam.common.repository.PaymentRepository;

@Service
public class PaymentService {

	@Autowired
    private PaymentRepository paymentRepository;

    /**
     * 결제 성공 시 회사 정보 저장
     */
    public Company saveCompanyInfo(Company company) {
        return paymentRepository.save(company);
    }
}
