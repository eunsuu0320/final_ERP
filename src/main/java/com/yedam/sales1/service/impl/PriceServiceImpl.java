package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Price;
import com.yedam.sales1.repository.PriceRepository;
import com.yedam.sales1.service.PriceService;

import jakarta.transaction.Transactional;

@Service
public class PriceServiceImpl implements PriceService {

	private final PriceRepository priceRepository;

	@Autowired
	public PriceServiceImpl(PriceRepository priceRepository) {
		this.priceRepository = priceRepository;
	}

	@Override
	public List<Price> getAllPrice() {
		return priceRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromPrice(List<Price> prices) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!prices.isEmpty()) {
			// 컬럼 정의
			columns.add("단가그룹코드");
			columns.add("단가그룹명");
			columns.add("단가");
			columns.add("단가유형");
			columns.add("사용구분");
			columns.add("비고");
			columns.add("거래처설정");
			columns.add("품목설정");

			for (Price price : prices) {
				Map<String, Object> row = new HashMap<>();
				row.put("단가그룹코드", price.getPriceGroupCode());
				row.put("단가그룹명", price.getPriceGroupName());
				row.put("단가", price.getPrice());
				row.put("단가유형", price.getPriceType());
				row.put("사용구분", price.getUsageStatus());
				row.put("비고", price.getRemarks());
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Price savePrice(Price price) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		String username = authentication.getName();
		
		String companyCode = username != null && username.contains(":") ? username.trim().split(":")[0].trim()
				: username.trim();

		price.setCompanyCode(companyCode);

		price.setPriceGroupCode(null);

		String newPriceGroupCode = generatePriceGroupCode();
		price.setPriceGroupCode(newPriceGroupCode);

		price.setPriceUniqueCode(null);

		Long newPriceUniqueCode = generatePriceUniqueCode();
		price.setPriceUniqueCode(newPriceUniqueCode);

		return priceRepository.save(price);
	}

	private String generatePriceGroupCode() {
		String priceGroupCode = priceRepository.findMaxPriceGroupCode();

		int nextNumber = 1;
		if (priceGroupCode != null) {
			String priceNumber = priceGroupCode.replaceAll("\\D", "");
			nextNumber = Integer.parseInt(priceNumber) + 1;
		}

		return String.format("PRICE%04d", nextNumber);
	}

	private Long generatePriceUniqueCode() {
		Long priceUniqueCode = priceRepository.findMaxPriceUniqueCode();
		return (priceUniqueCode == null) ? 1L : priceUniqueCode + 1;
	}

}
