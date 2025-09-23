package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.SalesStatement;

public interface SalesStatementRepository extends JpaRepository<SalesStatement, String> {}