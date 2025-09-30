package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Payment;

@Repository
public interface PartnerPaymentRepository extends
		JpaRepository<Payment, String>{

	List<Payment> findAll();
	
	@Query("SELECT MAX(p.paymentCode) FROM Payment p")
	String findMaxPaymentCode();

	Payment findByPaymentCode(String paymentCode);


}
