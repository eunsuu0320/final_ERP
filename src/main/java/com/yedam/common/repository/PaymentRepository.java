package com.yedam.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.Company;

@Repository
public interface PaymentRepository extends JpaRepository<Company, String> {
	
}
