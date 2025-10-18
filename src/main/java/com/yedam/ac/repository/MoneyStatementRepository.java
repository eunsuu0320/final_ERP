// src/main/java/com/yedam/ac/repository/MoneyStatementRepository.java
package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.MoneyStatement;

public interface MoneyStatementRepository extends JpaRepository<MoneyStatement, String> {
}
