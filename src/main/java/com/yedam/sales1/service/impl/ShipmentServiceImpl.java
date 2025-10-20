package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Shipment;
import com.yedam.sales1.domain.ShipmentDetail;
import com.yedam.sales1.dto.ShipmentRegistrationDTO;
import com.yedam.sales1.repository.OrderDetailRepository;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.repository.ShipmentDetailRepository;
import com.yedam.sales1.repository.ShipmentRepository;
import com.yedam.sales1.service.ShipmentService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j; // Slf4j 임포트 추가

// @Slf4j 어노테이션 추가 (log 변수를 사용하려면 필요)
@Service
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

	private final ShipmentRepository shipmentRepository;
	// ⭐ ShipmentDetail, Partner Repository 추가
	private final ShipmentDetailRepository shipmentDetailRepository;
	private final PartnerRepository partnerRepository;
	private final OrderDetailRepository orderDetailRepository;

	@Autowired
	public ShipmentServiceImpl(ShipmentRepository shipmentRepository, ShipmentDetailRepository shipmentDetailRepository,
			PartnerRepository partnerRepository, OrderDetailRepository orderDetailRepository) {
		this.shipmentRepository = shipmentRepository;
		this.shipmentDetailRepository = shipmentDetailRepository;
		this.partnerRepository = partnerRepository;
		this.orderDetailRepository = orderDetailRepository;

	}

	// =============================================================
	// 기본 CRUD 및 조회 로직
	// =============================================================

	@Override
	public List<Shipment> getAllShipment() {
		String companyCode = getCompanyCodeFromAuthentication();
		return shipmentRepository.findAll(companyCode);
	}

	@Override
	public List<Shipment> getFilterShipment(Shipment searchVo) {
		String companyCode = getCompanyCodeFromAuthentication();
		return shipmentRepository.findByFilter(searchVo, companyCode);
	}

	@Override
	public Map<String, Object> getTableDataFromShipments(List<Shipment> shipments) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = List.of("출하지시서코드", "출하예정일자", "거래처명", "창고명", "품목명", "수량합계", "담당자", "비고", "진행상태");

		for (Shipment shipment : shipments) {
			// 🔹 품목명 리스트 조회
			List<String> productNames = shipmentDetailRepository
					.findProductNamesByShipmentCode(shipment.getShipmentCode());

			// 🔹 대표 품목명 + 외 n건 처리
			String productSummary = "";
			if (productNames.isEmpty()) {
				productSummary = "";
			} else if (productNames.size() == 1) {
				productSummary = productNames.get(0);
			} else {
				productSummary = productNames.get(0) + " 외 " + (productNames.size() - 1) + "건";
			}

			// 🔹 거래처명 가져오기 (Shipment → Partner 연관관계 활용)
			String partnerName = "";
			if (shipment.getPartnerCode() != null) {
				partnerName = shipment.getPartner().getPartnerName();
			} else {
				partnerName = shipment.getPartnerCode(); // fallback
			}

			Map<String, Object> row = new HashMap<>();
			row.put("출하지시서코드", shipment.getShipmentCode());
			row.put("출하예정일자", shipment.getShipmentDate());
			row.put("담당자", shipment.getManagerEmp().getName());
			row.put("거래처명", partnerName);
			row.put("창고명", shipment.getWarehouse());
			row.put("품목명", productSummary);
			row.put("수량합계", shipment.getTotalQuantity());
			row.put("비고", shipment.getRemarks());
			row.put("진행상태", shipment.getStatus());

			rows.add(row);
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Shipment saveShipment(Shipment shipment) {
		return shipmentRepository.save(shipment);
	}

	@Override
	@Transactional
	public boolean updateShipmentStatus(String shipmentCode, String status) {
		log.info("Updating status for shipmentCode Code: {} to Status: {}", shipmentCode, status);

		// 1. EstimateCode로 견적서 엔티티를 조회합니다.
		Optional<Shipment> optionalShipment = shipmentRepository.findByShipmentCode(shipmentCode);

		if (optionalShipment.isEmpty()) {
			log.warn("Update failed: Shipment not found for code {}", shipmentCode);
			return false; // 견적서가 없으면 실패
		}

		Shipment shipment = optionalShipment.get();

		// 2. 상태를 업데이트합니다.
		shipment.setStatus(status);

		// 3. 변경 사항을 저장합니다. (Transactional 어노테이션 덕분에 save 호출 없이도 플러시될 수 있지만, 명시적으로 호출하는
		// 것이 안전합니다.)
		shipmentRepository.save(shipment);

		log.info("Shipment {} status successfully updated to {}", shipmentCode, status);
		return true;
	}

	// =============================================================
	// ⭐ Shipment 신규 등록 로직
	// =============================================================

	@Override
	@Transactional
	// Orders와 달리, PK가 String이므로 Long 대신 String을 반환하도록 수정했습니다.
	public String registerNewShipment(ShipmentRegistrationDTO dto) {

		// 1️⃣ 거래처 코드 조회 (DTO의 partnerName 기준)
		String partnerCode = getPartnerCodeByPartnerName(dto.getPartnerName());
		if (partnerCode == null) {
			throw new RuntimeException("유효하지 않거나 찾을 수 없는 거래처 이름입니다: " + dto.getPartnerName());
		}
		dto.setPartnerCode(partnerCode);

		// 2️⃣ 상세 항목 유효성 검사
		if (dto.getDetailList() == null || dto.getDetailList().isEmpty()) {
			throw new RuntimeException("출하 상세 항목이 누락되었습니다.");
		}

		// 3️⃣ 총 수량 서버에서 계산 및 엔티티 생성 준비
		Integer totalQuantity = calculateTotalQuantity(dto.getDetailList());
		String companyCode = getCompanyCodeFromAuthentication();

		// 4️⃣ 헤더 코드(ShipmentCode) 자동 부여 및 Shipment 엔티티 생성/저장
		String newShipmentCode = generateNewShipmentCode(); // ⭐ SHP0001 형식 생성

		Shipment shipment = createShipmentEntity(dto, newShipmentCode, totalQuantity, companyCode);
		shipmentRepository.save(shipment);

		// 5️⃣ 상세 항목 리스트 순회 및 저장 준비
		List<ShipmentDetail> newDetailsToSave = new ArrayList<>();

		// 상세 코드 생성을 위한 MaxCode 조회 (SHPD0001 형식)
		String maxDetailCode = shipmentDetailRepository.findMaxShipmentDetailCode();
		int detailNum = (maxDetailCode != null && maxDetailCode.startsWith("SHPD"))
				? (Integer.parseInt(maxDetailCode.substring(4)) + 1)
				: 1;

		// ✅ DTO에서 ShipmentDetail 엔티티 리스트를 받아 순회
		for (ShipmentDetail detail : dto.getDetailList()) {

			// ShipmentDetail 기본 세팅
			detail.setShipmentCode(newShipmentCode); // 마스터 코드 FK 설정
			detail.setCompanyCode(companyCode);
			detail.setStatus("지시 대기"); // 기본 상태
			String newDetailCode = String.format("SHPD%04d", detailNum++);
			detail.setShipmentDetailCode(newDetailCode);
			newDetailsToSave.add(detail);

			// ✅ 주문서 디테일(OrderDetail)의 미지시 수량(nonShipment) 갱신
			if (detail.getOrderDetailCode() != null && detail.getNonShipment() != null) {
				int updated = orderDetailRepository.updateNonShipmentByOrderDetailCode(detail.getOrderDetailCode(),
						detail.getNonShipment());

				if (updated == 0) {
					log.warn("⚠️ nonShipment 업데이트 실패 - orderUniqueCode: {}", detail.getOrderDetailCode());
				} else {
					log.info("✅ nonShipment 업데이트 완료 - orderUniqueCode: {}, nonShipment: {}",
							detail.getOrderDetailCode(), detail.getNonShipment());
				}
			} else {
				log.warn("⚠️ orderUniqueCode 또는 nonShipment 값이 비어 있어 업데이트 생략됨. detail: {}", detail);
			}
		}

		// 6️⃣ 리스트 전체를 한 번에 저장 (saveAll 사용)
		shipmentDetailRepository.saveAll(newDetailsToSave);

		log.info("🟢 새 출하 지시 등록 완료. 코드: {}", newShipmentCode);
		return newShipmentCode; // PK인 ShipmentCode 반환
	}

	// =============================================================
	// 헬퍼 메서드
	// =============================================================

	/** 헬퍼: 총 수량 계산 로직 */
	private Integer calculateTotalQuantity(List<ShipmentDetail> detailList) {
		return detailList.stream().filter(detail -> detail.getNowQuantity() != null)
				.mapToInt(ShipmentDetail::getNowQuantity).sum();
	}

	/** 헬퍼: Shipment 엔티티 생성 */
	private Shipment createShipmentEntity(ShipmentRegistrationDTO dto, String shipmentCode, Integer totalQuantity,
			String companyCode) {

		// DTO의 String 형태 날짜를 Date로 변환 (Shipment 엔티티는 Date 타입 사용)
		Date shipmentDate = java.sql.Date.valueOf(dto.getDeliveryDate());

		Date now = new Date();

		// Shipment 엔티티는 postCode가 Integer 타입이었으므로 변환합니다.
		Integer postCodeInt = null;
		if (dto.getPostCode() != null && !dto.getPostCode().isEmpty()) {
			try {
				postCodeInt = Integer.parseInt(dto.getPostCode());
			} catch (NumberFormatException e) {
				// 숫자로 변환할 수 없는 경우 null 처리 또는 예외 처리
				log.warn("Post Code is not a valid integer: {}", dto.getPostCode());
			}
		}

		return Shipment.builder().shipmentCode(shipmentCode).shipmentDate(shipmentDate)
				.partnerCode(dto.getPartnerCode()).warehouse(dto.getWarehouse()).totalQuantity(totalQuantity)
				.createDate(now) // 수동 설정
				.updateDate(now) // 수동 설정
				.manager(dto.getManager()).postCode(postCodeInt).address(dto.getAddress()).status("등록")
				.remarks(dto.getRemarks()).companyCode(companyCode).build();
	}

	/** 헬퍼: ShipmentCode 생성 (SHP0001 형식) */
	private String generateNewShipmentCode() {
		// shipmentRepository에 findMaxShipmentCode() 메서드가 정의되어 있어야 합니다.
		// OrdersRepository와 동일한 패턴으로 Repository에 정의되어 있다고 가정합니다.
		String maxCode = shipmentRepository.findMaxShipmentCode();
		String prefix = "SHP";
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
				log.error("Failed to parse existing shipment code number: {}", maxCode);
			}
		}
		return String.format("%s%04d", prefix, newNum);
	}

	/** 헬퍼: Partner Name으로 Partner Code를 조회합니다. */
	private String getPartnerCodeByPartnerName(String partnerName) {
		if (partnerName == null || partnerName.trim().isEmpty()) {
			return null;
		}
		// partnerRepository에 findByPartnerName 메서드가 정의되어 있다고 가정
		Partner partner = partnerRepository.findByPartnerName(partnerName);
		return (partner != null) ? partner.getPartnerCode() : null;
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