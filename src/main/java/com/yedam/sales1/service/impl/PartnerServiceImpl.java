package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication; 
import org.springframework.security.core.context.SecurityContextHolder; 
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Loan;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Payment;
import com.yedam.sales1.dto.PartnerRegistrationDTO;
import com.yedam.sales1.repository.LoanRepository;
import com.yedam.sales1.repository.PartnerPaymentRepository;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.service.PartnerService;

import jakarta.transaction.Transactional;

@Service
public class PartnerServiceImpl implements PartnerService {

	private final PartnerRepository partnerRepository;
	private final LoanRepository loanRepository;
	private final PartnerPaymentRepository partnerPaymentRepository;

	@Autowired
	public PartnerServiceImpl(PartnerRepository partnerRepository, LoanRepository loanRepository,
			PartnerPaymentRepository partnerPaymentRepository) {
		this.partnerRepository = partnerRepository;
		this.loanRepository = loanRepository;
		this.partnerPaymentRepository = partnerPaymentRepository;
	}

	@Override
	public List<Partner> getAllPartner() {
		return partnerRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromPartners(List<Partner> partners) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!partners.isEmpty()) {
			columns.add("거래처코드");
			columns.add("거래처명");
			columns.add("거래처유형");
			columns.add("사업자번호");
			columns.add("대표자");
			columns.add("전화번호");
			columns.add("업종");
			columns.add("업태");
			columns.add("이메일");
			columns.add("담당자");
			columns.add("사용여부");
			columns.add("비고");

			for (Partner partner : partners) {
				Map<String, Object> row = new HashMap<>();
				row.put("거래처코드", partner.getPartnerCode());
				row.put("거래처명", partner.getPartnerName());
				row.put("거래처유형", partner.getPartnerType());
				row.put("사업자번호", partner.getBusinessNo());
				row.put("대표자", partner.getCeoName());
				row.put("전화번호", partner.getPartnerPhone());
				row.put("업종", partner.getBusinessSector());
				row.put("업태", partner.getBusinessType());
				row.put("이메일", partner.getEmail());
				row.put("담당자", partner.getManager());
				row.put("사용여부", partner.getUsageStatus());
				row.put("비고", partner.getRemarks());
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Partner savePartner(Partner partner) {
		if (partner.getPartnerCode() == null || partner.getPartnerCode().isEmpty()) {
			String newCode = generateNewPartnerCode();
			partner.setPartnerCode(newCode);
		}
		
		return partnerRepository.save(partner);
	}

	@Override
	public Partner getPartnerByPartnerCode(String partnerCode) {
		return partnerRepository.findByPartnerCode(partnerCode);
	}

	@Override
	@Transactional
	public Partner saveFullPartnerData(PartnerRegistrationDTO partnerData) {

		Partner partner = partnerData.getPartnerData();
		Loan loan = partnerData.getLoanPriceData();
		List<Payment> payments = partnerData.getPaymentData();

		boolean isNewRegistration = (partner.getPartnerCode() == null || partner.getPartnerCode().isEmpty());
		
		if (isNewRegistration) {
			String companyCode = getCompanyCodeFromAuthentication();
			partner.setCompanyCode(companyCode);
			
			String newCode = generateNewPartnerCode();
			partner.setPartnerCode(newCode);
			
			if (partner.getUsageStatus() == null) {
			    partner.setUsageStatus("Y");
			}
		} 
		
		// 1. Partner 저장 (PK 확보)
		Partner savedPartner = partnerRepository.save(partner);
		String partnerCode = savedPartner.getPartnerCode();

		// 2. Loan 정보 저장
		if (loan != null) {
			loan.setPartnerCode(partnerCode);
			
			// 💡 FIX: Partner의 담당자 정보를 Loan의 MANAGER에 할당
			// (NULL 제약 조건 오류 해결)
			if (savedPartner.getManager() != null && !savedPartner.getManager().isEmpty()) {
				loan.setManager(savedPartner.getManager());
			} else {
				// Partner의 manager 필드가 비어있다면, DB 제약 조건을 피하기 위해 기본값 할당
				loan.setManager("담당자미지정"); 
			}
			
			// LoanCode 생성 및 할당
			if (loan.getLoanCode() == null || loan.getLoanCode().isEmpty()) {
				loan.setLoanCode(generateNewLoanCode());
			}

			loan.setCompanyCode(savedPartner.getCompanyCode());
			
			loanRepository.save(loan);
		}

		// 3. Payment 정보 저장
		if (payments != null && !payments.isEmpty()) {
			for (Payment payment : payments) {
				payment.setPartnerCode(partnerCode);

				if (payment.getPaymentCode() == null || payment.getPaymentCode().isEmpty()) {
					payment.setPaymentCode(generateNewPaymentCode());
				}
				
				payment.setCompanyCode(savedPartner.getCompanyCode());
			}
			partnerPaymentRepository.saveAll(payments);
		}

		return savedPartner;
	}

    /**
     * 현재 로그인된 사용자의 인증 정보에서 회사 코드를 추출합니다.
     */
    private String getCompanyCodeFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return "DEFAULT"; 
        }
        
        String username = authentication.getName();
        if (username != null && username.contains(":")) {
            return username.trim().split(":")[0].trim();
        }
        return username.trim();
    }


	// PartnerCode
	private String generateNewPartnerCode() {
		String maxCode = partnerRepository.findMaxPartnerCode();
		String prefix = "PART";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}

	// LoanCode
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
	

	// paymentCode
	private String generateNewPaymentCode() {
		String maxCode = partnerPaymentRepository.findMaxPaymentCode(); 
		String prefix = "PMNT";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}
}