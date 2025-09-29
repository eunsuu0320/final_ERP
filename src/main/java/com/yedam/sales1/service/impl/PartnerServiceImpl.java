package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.repository.PartnerRepository;
import com.yedam.sales1.service.PartnerService;

import jakarta.transaction.Transactional;

@Service
public class PartnerServiceImpl implements PartnerService {

	private final PartnerRepository partnerRepository;

	@Autowired
	public PartnerServiceImpl(PartnerRepository partnerRepository) {
		this.partnerRepository = partnerRepository;
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
			// 컬럼 정의
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
		// 거래처코드는 DB에서 자동 생성 또는 서비스에서 생성
		// 필요 시 partner.setPartnerCode(null);
		return partnerRepository.save(partner);
	}

	@Override
	public Partner getPartnerByPartnerCode(String PartnerCode) {
		return partnerRepository.findByPartnerCode(PartnerCode);
	}
}
