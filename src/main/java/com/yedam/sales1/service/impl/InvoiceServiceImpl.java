package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.EstimateDetail;
import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.InvoiceDetail;
import com.yedam.sales1.dto.InvoiceRegistrationDTO;
import com.yedam.sales1.repository.EstimateDetailRepository;
import com.yedam.sales1.repository.InvoiceDetailRepository;
import com.yedam.sales1.repository.InvoiceRepository;
import com.yedam.sales1.service.InvoiceService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

	private final InvoiceRepository invoiceRepository;
	private final InvoiceDetailRepository invoiceDetailRepository;

	@Autowired
	public InvoiceServiceImpl(InvoiceRepository invoiceRepository, InvoiceDetailRepository invoiceDetailRepository) {
		this.invoiceRepository = invoiceRepository;
		this.invoiceDetailRepository = invoiceDetailRepository;
	}

	@Override
	public List<Invoice> getAllInvoice() {
		return invoiceRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromInvoice(List<Invoice> invoices) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!invoices.isEmpty()) {
			// 컬럼 정의
			columns.add("청구서코드");
			columns.add("등록일자");
			columns.add("청구일자");
			columns.add("청구금액");
			columns.add("수금일자");
			columns.add("거래처명");
			columns.add("담당자");
			columns.add("진행상태");

			for (Invoice invoice : invoices) {
				Map<String, Object> row = new HashMap<>();
				row.put("청구서코드", invoice.getInvoiceCode());
				row.put("등록일자", invoice.getCreateDate());
				row.put("청구일자", invoice.getDmndDate());
				row.put("청구금액", invoice.getDmndAmt());
				row.put("수금일자", invoice.getRecptDate());
				row.put("거래처명", invoice.getPartnerCode());
				row.put("담당자", invoice.getManager());
				row.put("진행상태", invoice.getStatus());
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Invoice saveInvoice(Invoice invoice) {
		return invoiceRepository.save(invoice);
	}

	@Override
	@Transactional
	public boolean updateInvoiceStatus(String invoiceCode, String status) {
		log.info("Updating status for Invoice Code: {} to Status: {}", invoiceCode, status);

		// 1. EstimateCode로 견적서 엔티티를 조회합니다.
		Optional<Invoice> optionalInvoice = invoiceRepository.findByInvoiceCode(invoiceCode);

		if (optionalInvoice.isEmpty()) {
			log.warn("Update failed: Estimate not found for code {}", invoiceCode);
			return false; // 견적서가 없으면 실패
		}

		Invoice invoice = optionalInvoice.get();

		// 2. 상태를 업데이트합니다.
		invoice.setStatus(status);

		// 3. 변경 사항을 저장합니다. (Transactional 어노테이션 덕분에 save 호출 없이도 플러시될 수 있지만, 명시적으로 호출하는
		// 것이 안전합니다.)
		invoiceRepository.save(invoice);

		log.info("Estimate {} status successfully updated to {}", invoiceCode, status);
		return true;
	}

	
	@Override
	@Transactional
	public Long registerNewInvoice(InvoiceRegistrationDTO dto) {

		// 2. 상세 항목 유효성 검사
		if (dto.getInvoiceDetail() == null || dto.getInvoiceDetail().isEmpty()) {
			throw new RuntimeException("청구서 상세 항목이 누락되었습니다.");
		}
		System.out.println("=============================================================");
		System.out.println(dto);
		// 3. 총 금액 서버에서 재계산 및 엔티티 생성 준비
		Invoice invoice = createInvoiceEntity(dto);
		String companyCode = getCompanyCodeFromAuthentication();
		String manager = getManagerFromAuthentication();

		// 4. 헤더 코드 부여 및 저장 (PK 확보)
		String newCode = generateNewInvoiceCode(); // ESTxxxx
		invoice.setInvoiceCode(newCode);
		invoice.setCompanyCode(companyCode);
		invoice.setManager(manager);

		invoiceRepository.save(invoice);
		Long generatedInvoiceId = invoice.getInvoiceUniqueCode();

		// 5. 상세 항목 리스트 순회 및 저장 준비
		List<InvoiceDetail> newDetailsToSave = new ArrayList<>();

		// 상세 코드 생성을 위해 MaxCode 조회
		String maxDetailCode = invoiceDetailRepository.findMaxInvoiceDetailCode();
		int detailNum = (maxDetailCode != null && maxDetailCode.startsWith("ESD"))
				? (Integer.parseInt(maxDetailCode.substring(3)) + 1)
				: 1;

		for (InvoiceDetail detail : dto.getInvoiceDetail()) {
			// 새로운 엔티티 객체로 복사 (트랜잭션 충돌 방지 핵심)
			InvoiceDetail newDetail = InvoiceDetail.builder().shipmentCode(detail.getShipmentCode())
					.shipmentInvoiceAmount(detail.getShipmentInvoiceAmount()).loanInvoiceAmount(detail.getLoanInvoiceAmount()).loanInvoiceReason(detail.getLoanInvoiceReason()).build();

			newDetail.setCompanyCode(companyCode);

			// ESTIMATE_DETAIL_CODE 생성 및 할당 (수동 PK 할당)
			String newDetailCode = String.format("ISD%04d", detailNum++);
			newDetail.setInvoiceDetailCode(newDetailCode);

			newDetailsToSave.add(newDetail);
		}

		// 6. 리스트 전체를 한 번에 저장 (saveAll 사용)
		invoiceDetailRepository.saveAll(newDetailsToSave);

		log.info("새 견적서 등록 완료. ID: {}", generatedInvoiceId);
		return generatedInvoiceId;
	}
	
	
	
	
	
	
	/** 헬퍼: Invoice 엔티티 생성 */
	private Invoice createInvoiceEntity(InvoiceRegistrationDTO dto) {

		return Invoice.builder().partnerCode(dto.getPartnerCode())
				.partnerName(dto.getPartnerName())
				.dmndDate(dto.getDmndDate())
				.dmndAmt(dto.getDmndAmt())
				.status("미확인").build();
	}
	
	
	private String generateNewInvoiceCode() {
		String maxCode = invoiceRepository.findMaxInvoiceCode();
		String prefix = "INV";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}
	
	
	private String getCompanyCodeFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName().equals("anonymousUser")) {
			return "DEFAULT";
		}

		String username = authentication.getName();

		if (username != null && username.contains(":")) {
			return username.trim().split(":")[0].trim();
		}

		return "DEFAULT";
	}
	
	private String getManagerFromAuthentication() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || authentication.getName().equals("anonymousUser")) {
			return "DEFAULT";
		}

		String username = authentication.getName();

		if (username != null && username.contains(":")) {
			return username.trim().split(":")[2].trim();
		}

		return "DEFAULT";
	}



}
