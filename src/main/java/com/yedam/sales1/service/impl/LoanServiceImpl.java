package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Loan; 
import com.yedam.sales1.repository.LoanRepository; 
import com.yedam.sales1.service.LoanService;

import jakarta.transaction.Transactional;

@Service
public class LoanServiceImpl implements LoanService {

	private final LoanRepository loanRepository;

	@Autowired
	public LoanServiceImpl(LoanRepository loanRepository) {
		this.loanRepository = loanRepository;
	}

	@Override
	public List<Loan> getAllLoan() {
		return loanRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromLoans(List<Loan> loans) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		columns.add("여신코드");
		columns.add("거래처코드");
		columns.add("시작일");
		columns.add("종료일");
		columns.add("여신기간");
		columns.add("여신한도");
		columns.add("사용액");
		columns.add("결제예정일");
		columns.add("담당자");
		columns.add("상태");
		columns.add("비고");
		columns.add("현재버전");


		if (!loans.isEmpty()) {
			for (Loan loan : loans) {
				Map<String, Object> row = new HashMap<>();
				
				// Loan VO의 필드명(Camel Case)에 맞춰 데이터를 담습니다.
				row.put("여신코드", loan.getLoanCode());
				row.put("거래처코드", loan.getPartnerCode());
				row.put("시작일", loan.getLoanStartDate());
				row.put("종료일", loan.getLoanEndDate());
				row.put("여신기간", loan.getLoanTerm());
				row.put("여신한도", loan.getLoanLimit());
				row.put("사용액", loan.getLoanUse());
				row.put("결제예정일", loan.getLoanDay());
				row.put("담당자", loan.getManager());
				row.put("상태", loan.getStatus());
				row.put("비고", loan.getRemarks());
				row.put("현재버전", loan.getIsCurrentVersion());
				
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Loan saveLoan(Loan loan) {
		if (loan.getLoanCode() == null || loan.getLoanCode().isEmpty()) {
			String newCode = generateNewLoanCode();
			loan.setLoanCode(newCode);
		}
		
		return loanRepository.save(loan);
	}

	@Override
	public Loan getLoanByLoanCode(String keyword) {
		return loanRepository.findByLoanCode(keyword);
	}
	

	private String generateNewLoanCode() {
		String maxCode = loanRepository.findMaxLoanCode();
		String prefix = "LOAN";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}

	@Override
	public Loan findLoanDetailByPartner(String partnerCode) {
        return loanRepository.findByPartnerCode(partnerCode);

	}
}