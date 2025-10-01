package com.yedam.sales1.service.impl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.EstimateDetail;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.dto.EstimateRegistrationDTO;
import com.yedam.sales1.repository.EstimateDetailRepository;
import com.yedam.sales1.repository.EstimateRepository;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.service.EstimateService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class EstimateServiceImpl implements EstimateService {

	private final EstimateRepository estimateRepository;
	private final EstimateDetailRepository estimateDetailRepository;
	private final PartnerRepository partnerRepository;

	@Autowired
	public EstimateServiceImpl(EstimateRepository estimateRepository, EstimateDetailRepository estimateDetailRepository,
			PartnerRepository partnerRepository) {
		this.estimateRepository = estimateRepository;
		this.estimateDetailRepository = estimateDetailRepository;
		this.partnerRepository = partnerRepository;
	}

	// =============================================================
	// 1. 기본 조회 및 기타 메서드
	// =============================================================
	@Override
	public List<Estimate> getAllEstimate() {
		return estimateRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromEstimate(List<Estimate> estimates) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!estimates.isEmpty()) {
			// 컬럼 정의
			columns.add("견적서코드");
			columns.add("등록일자");
			columns.add("거래처명");
			columns.add("품목명");
			columns.add("유효기간");
			columns.add("견적금액합계");
			columns.add("담당자");
			columns.add("진행상태");

			for (Estimate estimate : estimates) {
				Map<String, Object> row = new HashMap<>();
				row.put("견적서코드", estimate.getEstimateCode());
				row.put("등록일자", estimate.getCreateDate());
				row.put("거래처명", estimate.getPartnerCode());
				row.put("품목명", estimate.getPartnerCode());
				row.put("유효기간", estimate.getExpiryDate());
				row.put("견적금액합계", estimate.getTotalAmount());
				row.put("담당자", estimate.getManager());
				row.put("진행상태", estimate.getStatus());
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Estimate saveEstimate(Estimate estimate) {
		return estimateRepository.save(estimate);
	}

	// =============================================================
	// 2. 신규 복합 등록 트랜잭션 로직 (최종 정리)
	// =============================================================
	@Override
	@Transactional
	public Long registerNewEstimate(EstimateRegistrationDTO dto) {

		// 1. 거래처 코드 조회 (PARTNER_CODE NULL 오류 해결)
		String partnerCode = getPartnerCodeByPartnerName(dto.getPartnerName());
		if (partnerCode == null) {
			throw new RuntimeException("유효하지 않거나 찾을 수 없는 거래처 이름입니다: " + dto.getPartnerName());
		}
		dto.setPartnerCode(partnerCode); // DTO에 코드 설정

		// 2. 상세 항목 유효성 검사
		if (dto.getDetailList() == null || dto.getDetailList().isEmpty()) {
			throw new RuntimeException("견적 상세 항목이 누락되었습니다.");
		}

		// 3. 총 금액 서버에서 재계산 및 엔티티 생성 준비
		Double totalAmount = calculateTotalAmount(dto.getDetailList());
		Estimate estimate = createEstimateEntity(dto, totalAmount);
		String companyCode = getCompanyCodeFromAuthentication();

		// 4. 헤더 코드 부여 및 저장 (PK 확보)
		String newCode = generateNewEstimateCode(); // ESTxxxx
		estimate.setEstimateCode(newCode);
		estimate.setCompanyCode(companyCode);
		estimateRepository.save(estimate);
		Long generatedEstimateId = estimate.getEstimateUniqueCode();

		// 5. 상세 항목 리스트 순회 및 저장 준비
		List<EstimateDetail> newDetailsToSave = new ArrayList<>();
		
		// 상세 코드 생성을 위해 MaxCode 조회
		String maxDetailCode = estimateDetailRepository.findMaxEstimateDetailCode();
		int detailNum = (maxDetailCode != null && maxDetailCode.startsWith("ESD"))
				? (Integer.parseInt(maxDetailCode.substring(3)) + 1)
				: 1;

		for (EstimateDetail detail : dto.getDetailList()) {
			// 새로운 엔티티 객체로 복사 (트랜잭션 충돌 방지 핵심)
			EstimateDetail newDetail = EstimateDetail.builder()
					.productCode(detail.getProductCode()).quantity(detail.getQuantity()).price(detail.getPrice())
					.remarks(detail.getRemarks())
					.build();

			// 외래 키(FK) 및 공통 필드 설정
			newDetail.setEstimateUniqueCode(generatedEstimateId);
			newDetail.setCompanyCode(companyCode);

			// ESTIMATE_DETAIL_CODE 생성 및 할당 (수동 PK 할당)
			String newDetailCode = String.format("ESD%04d", detailNum++);
			newDetail.setEstimateDetailCode(newDetailCode);

			newDetailsToSave.add(newDetail);
		}

		// 6. 리스트 전체를 한 번에 저장 (saveAll 사용)
		estimateDetailRepository.saveAll(newDetailsToSave);

		log.info("새 견적서 등록 완료. ID: {}", generatedEstimateId);
		return generatedEstimateId;
	}

	// =============================================================
	// 3. 필수 헬퍼 메서드
	// =============================================================

	/** 헬퍼: Estimate 엔티티 생성 */
	private Estimate createEstimateEntity(EstimateRegistrationDTO dto, Double totalAmount) {

		return Estimate.builder().partnerCode(dto.getPartnerCode())
				.createDate(Date.from(dto.getQuoteDate().atStartOfDay(ZoneId.systemDefault()).toInstant()))
				.expiryDate(dto.getQuoteDate().plusDays(dto.getValidPeriod()).toString()).totalAmount(totalAmount)
				.manager(dto.getManager()).status("미확인").remarks(dto.getRemarks()).build();
	}

	/** 헬퍼: 총 금액 계산 로직 (보안 및 신뢰성 확보) */
	private Double calculateTotalAmount(List<EstimateDetail> detailList) {
		double totalSum = 0.0;
		for (EstimateDetail detail : detailList) {
			totalSum += (double) detail.getQuantity() * (double) detail.getPrice() * 1.1;
		}
		return Math.round(totalSum * 100.0) / 100.0;
	}

	/** 헬퍼: EstimateCode 생성 */
	private String generateNewEstimateCode() {
		String maxCode = estimateRepository.findMaxEstimateCode();
		String prefix = "EST";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}
	
	/** 헬퍼: Partner Name으로 Partner Code를 조회합니다. */
	private String getPartnerCodeByPartnerName(String partnerName) {
		if (partnerName == null || partnerName.trim().isEmpty()) {
			return null;
		}

		Partner partner = partnerRepository.findByPartnerName(partnerName);

		if (partner != null) {
			return partner.getPartnerCode();
		}
		return null;
	}

	/** 헬퍼: Security 인증 정보에서 회사 코드를 추출 */
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
}