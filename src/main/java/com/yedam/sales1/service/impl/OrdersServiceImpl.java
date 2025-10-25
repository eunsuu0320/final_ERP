package com.yedam.sales1.service.impl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales1.domain.Estimate;
import com.yedam.sales1.domain.OrderDetail;
import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.dto.OrderModalDTO;
import com.yedam.sales1.dto.OrderRegistrationDTO;
import com.yedam.sales1.repository.EstimateRepository;
import com.yedam.sales1.repository.OrderDetailRepository;
import com.yedam.sales1.repository.OrdersRepository;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.repository.ProductRepository;
import com.yedam.sales1.service.OrdersService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrdersServiceImpl implements OrdersService {

    private final OrdersRepository ordersRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final PartnerRepository partnerRepository;
    private final EstimateRepository estimateRepository;
    private final ProductRepository productRepository;

    @Autowired
    public OrdersServiceImpl(OrdersRepository ordersRepository,
                             OrderDetailRepository orderDetailRepository,
                             PartnerRepository partnerRepository,
                             EstimateRepository estimateRepository,
                             ProductRepository productRepository) {
        this.ordersRepository = ordersRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.partnerRepository = partnerRepository;
        this.estimateRepository = estimateRepository;
        this.productRepository = productRepository;
    }

    @Override
    public List<Orders> getAllOrders() {
        return ordersRepository.findAll();
    }

    @Override
    public List<Orders> getFilterOrder(Orders searchVo) {
        return ordersRepository.findByFilter(searchVo);
    }

    // ✅ 메인 테이블용 변환 (요구사항 7~11 반영)
    @Override
    public Map<String, Object> getTableDataFromOrders(List<Orders> list) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = Arrays.asList(
            "주문서고유코드", "주문서코드", "등록일자", "거래처명",
            "담당자", "품목명", "납기일자", "주문금액합계", "우편번호", "상세주소", "결제조건", "견적서코드", "진행상태"
        );

        for (Orders o : list) {
            // 거래처명 (요구 7)
            String partnerName = (o.getPartner() != null)
                    ? o.getPartner().getPartnerName()
                    : o.getPartnerCode();

            // 담당자명 (요구 8)
            String managerName = (o.getManagerEmp() != null)
                    ? o.getManagerEmp().getName()
                    : o.getManager();

            // 품목명 요약 (요구 9)
            List<OrderDetail> details = orderDetailRepository.findByOrderUniqueCode(o.getOrderUniqueCode());
            String productSummary = "";
            if (details != null && !details.isEmpty()) {
                List<String> names = new ArrayList<>();
                for (OrderDetail d : details) {
                    Product p = productRepository.findById(d.getProductCode()).orElse(null);
                    if (p != null && p.getProductName() != null) names.add(p.getProductName());
                }
                if (names.size() == 1) productSummary = names.get(0);
                else if (names.size() > 1) productSummary = names.get(0) + " 외 " + (names.size() - 1) + "건";
            }

            // 견적서코드 (요구 10)
            String estimateCode = null;
            if (o.getEstimateUniqueCode() != null) {
                Estimate est = estimateRepository.findByEstimateUniqueCode(o.getEstimateUniqueCode());
                estimateCode = (est != null) ? est.getEstimateCode() : null;
            }

            Map<String, Object> row = new HashMap<>();
            row.put("주문서고유코드", o.getOrderUniqueCode());
            row.put("주문서코드", o.getOrderCode());
            row.put("등록일자", o.getCreateDate());
            row.put("거래처명", partnerName);
            row.put("담당자", managerName);
            row.put("품목명", productSummary);
            row.put("납기일자", o.getDeliveryDate());
            row.put("주문금액합계", o.getTotalAmount());
            row.put("견적서코드", estimateCode);
            row.put("진행상태", o.getStatus());
            row.put("우편번호", o.getPostCode());
            row.put("상세주소", o.getAddress());
            row.put("결제조건", o.getPayCondition());

            rows.add(row);
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Long registerNewOrders(OrderRegistrationDTO dto) {
        // 거래처 코드 보정
        String partnerCode = getPartnerCodeByPartnerName(dto.getPartnerName());
        if (partnerCode == null)
            throw new RuntimeException("유효하지 않은 거래처명: " + dto.getPartnerName());
        dto.setPartnerCode(partnerCode);

        if (dto.getDetailList() == null || dto.getDetailList().isEmpty())
            throw new RuntimeException("주문 상세 항목이 누락되었습니다.");

        Double totalAmount = calculateTotalAmount(dto.getDetailList());
        Orders orders = createOrderEntity(dto, totalAmount);

        String newOrderCode = generateNewOrderCode();
        orders.setOrderCode(newOrderCode);
        orders.setCompanyCode(getCompanyCodeFromAuthentication());
        orders.setWriter(getManagerFromAuthentication());

        ordersRepository.save(orders);
        Long orderUk = orders.getOrderUniqueCode();

        String maxDetailCode = orderDetailRepository.findMaxOrderDetailCode();
        int detailNum = (maxDetailCode != null && maxDetailCode.startsWith("ORDD"))
                ? (Integer.parseInt(maxDetailCode.substring(4)) + 1)
                : 1;

        List<OrderDetail> saveList = new ArrayList<>();
        for (OrderDetail d : dto.getDetailList()) {
            OrderDetail nd = OrderDetail.builder()
                    .productCode(d.getProductCode())
                    .quantity(d.getQuantity())
                    .price(d.getPrice())
                    .amountSupply(d.getAmountSupply())
                    .pctVat(d.getPctVat())
                    .remarks(d.getRemarks())
                    .status("등록")
                    .nonShipment(d.getQuantity())
                    .build();
            nd.setOrderUniqueCode(orderUk);
            nd.setCompanyCode(orders.getCompanyCode());
            nd.setOrderDetailCode(String.format("ORDD%04d", detailNum++));
            saveList.add(nd);
        }

        orderDetailRepository.saveAll(saveList);
        log.info("새 주문서 등록 완료. ID: {}", orderUk);
        return orderUk;
    }

    private Double calculateTotalAmount(List<OrderDetail> detailList) {
        double total = 0.0;
        for (OrderDetail d : detailList) {
            if (d.getAmountSupply() != null && d.getPctVat() != null)
                total += d.getAmountSupply() * (1 + (d.getPctVat() / 100.0));
            else if (d.getQuantity() != null && d.getPrice() != null)
                total += d.getQuantity() * d.getPrice() * 1.1;
        }
        return Math.round(total * 100.0) / 100.0;
    }

    private Orders createOrderEntity(OrderRegistrationDTO dto, Double totalAmount) {
        Date orderDate = dto.getOrderDate() != null
                ? Date.from(dto.getOrderDate().atStartOfDay(ZoneId.systemDefault()).toInstant())
                : new Date();

        return Orders.builder()
                .partnerCode(dto.getPartnerCode())
                .estimateUniqueCode(dto.getEstimateUniqueCode())
                .createDate(orderDate)
                .deliveryDate(dto.getDeliveryDate())
                .totalAmount(totalAmount)
                .manager(dto.getManager())
                .status("미확인")
                .remarks(dto.getRemarks())
                .postCode(dto.getPostCode())
                .address(dto.getAddress())
                .payCondition(dto.getPayCondition())
                .build();
    }

    private String generateNewOrderCode() {
        String maxCode = ordersRepository.findMaxOrdersCode();
        String prefix = "ORD";
        int newNum = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            try { newNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1; }
            catch (NumberFormatException ignored) {}
        }
        return String.format("%s%04d", prefix, newNum);
    }

    private String getPartnerCodeByPartnerName(String name) {
        if (name == null || name.trim().isEmpty()) return null;
        Partner p = partnerRepository.findByPartnerName(name);
        return (p != null) ? p.getPartnerCode() : null;
    }

    private String getCompanyCodeFromAuthentication() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) return "DEFAULT";
        String username = auth.getName();
        if (username != null && username.contains(":")) return username.trim().split(":")[0].trim();
        return "DEFAULT";
    }

    private String getManagerFromAuthentication() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || "anonymousUser".equals(auth.getName())) return "DEFAULT";
        String username = auth.getName();
        if (username != null && username.contains(":")) return username.trim().split(":")[2].trim();
        return "DEFAULT";
    }

    @Override
    public OrderModalDTO getOrderModalByOrderUniqueCode(Long orderUniqueCode) {
        Optional<Orders> opt = ordersRepository.findById(orderUniqueCode);
        if (opt.isEmpty()) return null;

        Orders o = opt.get();

        OrderModalDTO dto = new OrderModalDTO();
        dto.setOrderUniqueCode(o.getOrderUniqueCode());
        dto.setOrderCode(o.getOrderCode());
        dto.setPartnerCode(o.getPartnerCode());
        dto.setPartnerName(o.getPartner() != null ? o.getPartner().getPartnerName() : null);
        dto.setManager(o.getManager());
        dto.setManagerName(o.getManagerEmp() != null ? o.getManagerEmp().getName() : null);
        dto.setDeliveryDate(o.getDeliveryDate());
        dto.setRemarks(o.getRemarks());
        dto.setEstimateUniqueCode(o.getEstimateUniqueCode());
        dto.setPostCode(o.getPostCode());
        dto.setAddress(o.getAddress());
        dto.setPayCondition(o.getPayCondition());

        // ✅ 견적 코드 매핑
        if (o.getEstimateUniqueCode() != null) {
            Estimate est = estimateRepository.findByEstimateUniqueCode(o.getEstimateUniqueCode());
            if (est != null) dto.setEstimateCode(est.getEstimateCode());
        }

        // ✅ 상세 리스트 그대로 엔티티로 세팅
        List<OrderDetail> details = orderDetailRepository.findByOrderUniqueCode(orderUniqueCode);
        dto.setDetailList(details);   // ⚡️ 이제 타입이 일치하므로 정상 작동

        return dto;
    }


    @Override
    @Transactional
    public boolean updateOrdersStatus(String orderCode, String status) {
        log.info("Updating status for Order Code: {} to Status: {}", orderCode, status);
        Optional<Orders> optionalOrder = ordersRepository.findByOrderCode(orderCode);
        if (optionalOrder.isEmpty()) {
            log.warn("Update failed: Order not found for code {}", orderCode);
            return false;
        }

        Orders orders = optionalOrder.get();
        orders.setStatus(status);
        ordersRepository.save(orders);
        log.info("Order {} status successfully updated to {}", orderCode, status);
        return true;
    }
}
