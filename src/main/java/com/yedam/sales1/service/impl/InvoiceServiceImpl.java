package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.InvoiceDetail;
import com.yedam.sales1.dto.InvoiceRegistrationDTO;
import com.yedam.sales1.dto.InvoiceResponseDto;
import com.yedam.sales1.dto.InvoiceSaveRequestDto;
import com.yedam.sales1.repository.InvoiceDetailRepository;
import com.yedam.sales1.repository.InvoiceRepository;
import com.yedam.sales1.service.InvoiceService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceDetailRepository invoiceDetailRepository;

    @Autowired
    public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
                              InvoiceDetailRepository invoiceDetailRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceDetailRepository = invoiceDetailRepository;
    }

    // ===============================
    // 조회 유틸
    // ===============================
    @Override
    public List<Invoice> getAllInvoice() {
        return invoiceRepository.findAll();
    }

    @Override
    public List<Invoice> getFilterInvoice(Invoice searchVo) {
        return invoiceRepository.findByFilter(searchVo);
    }

    @Override
    public Map<String, Object> getTableDataFromInvoice(List<Invoice> invoices) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!invoices.isEmpty()) {
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
                row.put("거래처명", invoice.getPartnerName());
                row.put("담당자", invoice.getManagerEmp().getName());
                row.put("진행상태", invoice.getStatus());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    // ===============================
    // 저장 (엔티티 직접)
    // ===============================
    @Override
    @Transactional
    public Invoice saveInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    // ===============================
    // ✅ 청구서 등록 (DTO)
    // ===============================
    @Override
    @Transactional
    public void saveInvoice(InvoiceSaveRequestDto dto) {
        // 1) 회사코드
        String companyCode = getCompanyCodeFromAuthentication();

        // 2) 청구서코드 생성
        String invoiceCode = generateInvoiceCode();

        // 3) 헤더 저장
        Invoice invoice = dto.toEntity(invoiceCode);
        invoice.setCompanyCode(companyCode);
        invoiceRepository.save(invoice); // PK(INVOICE_UNIQUE_CODE) 생성

        // 4) 상세 저장
        for (InvoiceSaveRequestDto.InvoiceDetailDto d : dto.getInvoiceDetail()) {
            String newDetailCode = generateInvoiceDetailCode();

            InvoiceDetail detail = InvoiceDetail.builder()
                    .invoiceDetailCode(newDetailCode)
                    .invoiceUniqueCode(invoice.getInvoiceUniqueCode().intValue())
                    .shipmentCode(d.getShipmentCode())
                    .shipmentDate(d.getShipmentDate())     // ✅ 출하일 추가
                    .quantity(d.getQuantity())             // ✅ 전체수량 추가
                    .totalAmount(d.getTotalAmount())       // ✅ 공급가액 추가
                    .tax(d.getTax())                       // ✅ 부가세 추가
                    .shipmentInvoiceAmount(d.getShipmentInvoiceAmount()) // ✅ 최종금액(합계)
                    .shipmentInvoiceAmount(d.getShipmentInvoiceAmount())
                    .companyCode(companyCode)
                    .build();

            invoiceDetailRepository.save(detail);
        }
    }

    // ===============================
    // ✅ 청구서 상세조회
    // ===============================
    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDto getInvoiceDetail(String invoiceCode) {
        log.info("🔍 [getInvoiceDetail] 호출됨: invoiceCode={}", invoiceCode);

        try {
            Invoice invoice = invoiceRepository.findByInvoiceCode(invoiceCode)
                    .orElseThrow(() -> new RuntimeException("❌ 청구서 정보를 찾을 수 없습니다."));

            log.info("✅ 청구서 조회 성공: uniqueCode={}", invoice.getInvoiceUniqueCode());

            List<InvoiceDetail> details = invoiceDetailRepository.findByInvoiceUniqueCode(
                    invoice.getInvoiceUniqueCode().intValue()
            );

            log.info("📦 디테일 항목 개수={}", (details != null ? details.size() : 0));
            if (details != null) {
                for (InvoiceDetail d : details) {
                    log.info("  🔸 {} | 금액 {}", d.getShipmentCode(), d.getShipmentInvoiceAmount());
                }
            }

            InvoiceResponseDto dto = InvoiceResponseDto.from(invoice, details);
            log.info("🎯 DTO 변환 완료: {}", dto);
            return dto;

        } catch (Exception e) {
            log.error("🚨 [getInvoiceDetail] 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }


    // ===============================
    // ✅ 상태 변경
    // ===============================
    @Override
    @Transactional
    public boolean updateInvoiceStatus(String invoiceCode, String status) {
        log.info("Updating status for Invoice Code: {} -> {}", invoiceCode, status);

        return invoiceRepository.findByInvoiceCode(invoiceCode)
                .map(inv -> {
                    inv.setStatus(status);
                    invoiceRepository.save(inv);
                    return true;
                })
                .orElse(false);
    }

    // ===============================
    // 헬퍼: 코드 생성
    // ===============================

    /** 청구서코드: INV0001, INV0002 ...  */
    private String generateInvoiceCode() {
        String prefix = "INV";
        String maxCode = invoiceRepository.findMaxInvoiceCode(); // ex) INV0032
        int next = 1;
        if (maxCode != null && maxCode.startsWith(prefix)) {
            try {
                next = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("%s%04d", prefix, next);
    }

    /** 상세코드: INVD0001, INVD0002 ... */
    private String generateInvoiceDetailCode() {
        String prefix = "INVD";
        String last = invoiceDetailRepository.findMaxInvoiceDetailCode(); // ex) INVD0123
        int next = 1;
        if (last != null && last.startsWith(prefix)) {
            try {
                next = Integer.parseInt(last.substring(prefix.length())) + 1;
            } catch (NumberFormatException ignored) {}
        }
        return String.format("%s%04d", prefix, next);
    }

    // ===============================
    // 헬퍼: 회사코드 추출
    // ===============================
    private String getCompanyCodeFromAuthentication() {
        var auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();

        if (auth == null || "anonymousUser".equals(auth.getName())) return "DEFAULT";

        String username = auth.getName(); // 예: "C001:admin01:0001:..."
        if (username != null && username.contains(":")) {
            return username.trim().split(":")[0].trim();
        }
        return "DEFAULT";
    }

	@Override
	public Long registerNewInvoice(InvoiceRegistrationDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}
}
