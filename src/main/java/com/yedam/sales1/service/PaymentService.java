package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Payment;

public interface PaymentService {
	List<Payment> getAllPayment();

	Map<String, Object> getTableDataFromPayments(List<Payment> payments);
	
	Payment savePayment(Payment payment);

	Payment getPartnerByPaymentCode(String keyword);
	
    List<Payment> findPaymentsByPartnerCode(String partnerCode);

}
