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

import com.yedam.sales1.domain.OrderDetail;
import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.dto.OrderRegistrationDTO;
import com.yedam.sales1.repository.OrderDetailRepository;
import com.yedam.sales1.repository.OrdersRepository;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.service.OrdersService;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j; 

@Service
@Slf4j
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderDetailRepository orderDetailRepository;
	private final PartnerRepository partnerRepository;

    @Autowired
    public OrdersServiceImpl(OrdersRepository ordersRepository, OrderDetailRepository orderDetailRepository, PartnerRepository partnerRepository) {
        this.ordersRepository = ordersRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.partnerRepository = partnerRepository;
    }

    @Override
    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    @Override
    public Map<String, Object> getTableDataFromOrders(List<Orders> orders) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!orders.isEmpty()) {
            columns.add("주문서코드");
            columns.add("등록일자");
            columns.add("거래처명");
            columns.add("담당자");
            columns.add("품목명");
            columns.add("납기일자");
            columns.add("주문금액합계");
            columns.add("견적서코드");
            columns.add("진행상태");

            for (Orders order : orders) {
                Map<String, Object> row = new HashMap<>();
                row.put("주문서코드", order.getOrderCode());
                row.put("등록일자", order.getCreateDate());
                row.put("거래처명", order.getPartnerCode()); 
                row.put("담당자", order.getManager());
                row.put("품목명", "상세 항목 수 등 표시 로직 필요"); 
                row.put("납기일자", order.getDeliveryDate());
                row.put("주문금액합계", order.getTotalAmount());
                row.put("견적서코드", order.getEstimateUniqueCode());
                row.put("진행상태", order.getStatus());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Orders saveOrders(Orders orders) {
        return ordersRepository.save(orders);
    }

	
	@Override
	@Transactional
	public Long registerNewOrders(OrderRegistrationDTO dto) {

        // 1. 거래처 코드 조회 
		String partnerCode = getPartnerCodeByPartnerName(dto.getPartnerName());
		if (partnerCode == null) {
			throw new RuntimeException("유효하지 않거나 찾을 수 없는 거래처 이름입니다: " + dto.getPartnerName());
		}
		dto.setPartnerCode(partnerCode);

		// 2. 상세 항목 유효성 검사
		if (dto.getDetailList() == null || dto.getDetailList().isEmpty()) {
			throw new RuntimeException("주문 상세 항목이 누락되었습니다.");
		}

		// 3. 총 금액 서버에서 재계산 및 엔티티 생성 준비
		// ⭐⭐ 이 메서드가 누락되어 에러가 발생했습니다.
		Double totalAmount = calculateTotalAmount(dto.getDetailList()); 
        
		Orders orders = createOrderEntity(dto, totalAmount);
		String companyCode = getCompanyCodeFromAuthentication();

		// 4. 헤더 코드(orderCode) 자동 부여 및 저장
		String newOrderCode = generateNewOrderCode(); 
		orders.setOrderCode(newOrderCode);
		orders.setCompanyCode(companyCode);
        
        // save 호출 시 orderUniqueCode (PK)는 시퀀스에 의해 자동 할당됩니다.
		ordersRepository.save(orders);
		Long generatedOrderUniqueCode = orders.getOrderUniqueCode(); 

		// 5. 상세 항목 리스트 순회 및 저장 준비
		List<OrderDetail> newDetailsToSave = new ArrayList<>();
		
		String maxDetailCode = orderDetailRepository.findMaxOrderDetailCode();
		int detailNum = (maxDetailCode != null && maxDetailCode.startsWith("ORDD"))
				? (Integer.parseInt(maxDetailCode.substring(4)) + 1)
				: 1;

		for (OrderDetail detail : dto.getDetailList()) {
			
			// OrderDetail.builder() 사용
			OrderDetail newDetail = OrderDetail.builder() 
					.productCode(detail.getProductCode())
                    .quantity(detail.getQuantity())
                    .price(detail.getPrice())
                    .amountSupply(detail.getAmountSupply()) 
                    .pctVat(detail.getPctVat())
					.remarks(detail.getRemarks())
                    .status("등록") 
					.build();

			// 외래 키(FK) 및 공통 필드 설정
			newDetail.setOrderUniqueCode(generatedOrderUniqueCode); 
			newDetail.setCompanyCode(companyCode);

			// ORDER_DETAIL_CODE 생성 및 할당 (수동 PK 할당)
			String newDetailCode = String.format("ORDD%04d", detailNum++);
			newDetail.setOrderDetailCode(newDetailCode);

			newDetailsToSave.add(newDetail);
		}

		// 6. 리스트 전체를 한 번에 저장 (saveAll 사용)
		orderDetailRepository.saveAll(newDetailsToSave);

		log.info("새 주문서 등록 완료. ID: {}", generatedOrderUniqueCode);
		return generatedOrderUniqueCode;
	}

	// =============================================================
	// 3. 필수 헬퍼 메서드
	// =============================================================
	
	/** 헬퍼: 총 금액 계산 로직 (보안 및 신뢰성 확보) */
    // ⭐⭐ 누락된 메서드 추가
	private Double calculateTotalAmount(List<OrderDetail> detailList) {
		double totalSum = 0.0;
		for (OrderDetail detail : detailList) {
            // 공급가액(amountSupply)과 부가세율(pctVat)을 사용하는 경우
            if (detail.getAmountSupply() != null && detail.getPctVat() != null) {
                totalSum += detail.getAmountSupply() * (1 + (detail.getPctVat() / 100.0));
            } 
            // 수량(quantity)과 단가(price)를 사용하는 경우 (VAT 10% 가정)
            else if (detail.getQuantity() != null && detail.getPrice() != null) {
                totalSum += (double) detail.getQuantity() * (double) detail.getPrice() * 1.1;
            }
		}
        // 소수점 셋째 자리에서 반올림
		return Math.round(totalSum * 100.0) / 100.0;
	}


	/** 헬퍼: Order 엔티티 생성 */
	private Orders createOrderEntity(OrderRegistrationDTO dto, Double totalAmount) {
        
        // LocalDate를 Date로 변환하는 로직:
        Date orderDate = Date.from(dto.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date deliveryDate = Date.from(dto.getDeliveryDate().atStartOfDay(ZoneId.systemDefault()).toInstant());
        
		return Orders.builder()
                .partnerCode(dto.getPartnerCode())
                .estimateUniqueCode(dto.getEstimateUniqueCode())
				.createDate(orderDate)
                .deliveryDate(deliveryDate) 
				.totalAmount(totalAmount)
				.manager(dto.getManager())
                .status("주문 등록") 
                .remarks(dto.getRemarks())
                .build();
	}

	/** 헬퍼: OrderCode 생성 (ORD0001 형식) */
	private String generateNewOrderCode() { 
		String maxCode = ordersRepository.findMaxOrdersCode(); 
		String prefix = "ORD"; 
		int newNum = 1;

		if (maxCode != null && maxCode.startsWith(prefix)) {
			try {
				newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
			} catch (NumberFormatException e) {
                log.error("Failed to parse existing order code number: {}", maxCode);
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