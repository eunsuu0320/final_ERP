package com.yedam.common.service;

import java.util.Calendar;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.common.domain.Company;
import com.yedam.common.domain.Subscription;
import com.yedam.common.domain.payment.PayRequest;
import com.yedam.common.repository.PaymentRepository;
import com.yedam.common.repository.SubscriptionRepository;

@Service
public class PaymentService {

	@Autowired
    private PaymentRepository paymentRepository;
	
	@Autowired
    private SubscriptionRepository subscriptionRepository;
	
    /*
     * 결제 성공 시 회사 정보 저장
     */
    public Company saveCompanyInfo(Company company) {
        return paymentRepository.save(company);
    }
    
    /*
     * 결제 성공 시 회사 정보 저장
     */
	public Subscription saveSubscriptionInfo(PayRequest request, String companyCode) {
        Date startDate = new Date();

        // request.getSubPeriod() 예: "3개월"
        int months = 1; // 기본 1개월
        if (request.getSubPeriod() != null && request.getSubPeriod().contains("개월")) {
            months = Integer.parseInt(request.getSubPeriod().replace("개월", "").trim());
        }

        // 만료일 계산 (현재 날짜 + months 개월)
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        cal.add(Calendar.MONTH, months);
        Date endDate = cal.getTime();

        Subscription subscription = new Subscription();
        subscription.setCompanyCode(companyCode);
        subscription.setSubscriptionStartDate(startDate);
        subscription.setSubscriptionEndDate(endDate);
        subscription.setPeriodMonth(request.getSubPeriod());
        subscription.setPrice((long) request.getAmount());
        subscription.setStatus("구독중");

        return subscriptionRepository.save(subscription);
    }
}
