package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Loan;

@Repository
public interface LoanRepository extends
		JpaRepository<Loan, Long>{

	List<Loan> findAll();
	
	@Query("SELECT MAX(p.loanCode) FROM Loan p")
	String findMaxLoanCode();

	Loan findByLoanCode(String loanCode);
	
    Loan findByPartnerCode(String partnerCode);


}
