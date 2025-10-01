package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Payment;
import com.yedam.sales1.repository.PartnerPaymentRepository;
import com.yedam.sales1.service.PaymentService;

import jakarta.transaction.Transactional;

@Service
public class PaymentServiceImpl implements PaymentService {

	private final PartnerPaymentRepository paymentRepository;

	@Autowired
	public PaymentServiceImpl(PartnerPaymentRepository paymentRepository) {
		this.paymentRepository = paymentRepository;
	}

	@Override
	public List<Payment> getAllPayment() {
		return paymentRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromPayments(List<Payment> payments) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		columns.add("결제코드");
		columns.add("거래처코드");
		columns.add("은행코드");
		columns.add("은행명");
		columns.add("계좌번호");
		columns.add("예금주명");
		columns.add("기본여부");
		columns.add("사용여부");
		columns.add("회사코드");

		if (!payments.isEmpty()) {
			for (Payment payment : payments) {
				Map<String, Object> row = new HashMap<>();
				row.put("결제코드", payment.getPaymentCode());
				row.put("거래처코드", payment.getPartnerCode());
				row.put("은행코드", payment.getBankCode());
				row.put("은행명", payment.getBankName());
				row.put("계좌번호", payment.getAccountNo());
				row.put("예금주명", payment.getDepositorName());
				row.put("기본여부", payment.getIsDefault());
				row.put("사용여부", payment.getUsageStatus());
				row.put("회사코드", payment.getCompanyCode());
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Payment savePayment(Payment payment) {
		// 저장 전 PaymentCode 자동 생성 로직
		if (payment.getPaymentCode() == null || payment.getPaymentCode().isEmpty()) {
			String newCode = generateNewPaymentCode();
			payment.setPaymentCode(newCode);
		}
		
		return paymentRepository.save(payment);
	}

	@Override
	public Payment getPartnerByPaymentCode(String keyword) {
		return paymentRepository.findByPaymentCode(keyword);
	}

	private String generateNewPaymentCode() {
		String maxCode = paymentRepository.findMaxPaymentCode();
		String prefix = "PAY";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}
}