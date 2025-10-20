package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Loan;

public interface LoanService {
	List<Loan> getAllLoan();

	Map<String, Object> getTableDataFromLoans(List<Loan> loans);
	
	Loan saveLoan(Loan loan);

	Loan getLoanByLoanCode(String keyword);
	
    Loan findLoanDetailByPartner(String partnerCode);

}
