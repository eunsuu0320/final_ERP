package com.yedam.ac.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.ac.domain.Statement;

public interface StatementRepository extends JpaRepository<Statement, String> {}