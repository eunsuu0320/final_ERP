// src/main/java/com/yedam/ac/repository/PaymentStatementRepository.java
package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.PaymentStatement;

public interface PaymentStatementRepository extends JpaRepository<PaymentStatement, String> {
}
