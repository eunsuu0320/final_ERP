package com.yedam.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.common.domain.Company;

public interface CompanyRepository extends JpaRepository<Company, String> {
	
	 Optional<Company> findById(String companyCode);
}
