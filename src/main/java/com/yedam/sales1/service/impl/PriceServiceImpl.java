package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;
import com.yedam.sales1.domain.PriceDetail;
import com.yedam.sales1.domain.Product;
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
		return priceRepository.findAllWithAllRelations();
	}


	@Override
	public Map<String, Object> getTableDataFromPartners(List<Price> prices) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!prices.isEmpty()) {
			// 1. 컬럼 정의
			columns.add("거래처명");
			columns.add("거래처코드");
			columns.add("거래처유형");
			columns.add("단가그룹코드");
			columns.add("단가그룹명");
			columns.add("단가유형");
			columns.add("할인율");
			columns.add("단가적용시작일");
			columns.add("단가적용종료일");
			columns.add("사용구분");
			columns.add("비고");

			// 2. Price 리스트 순회 (상위 그룹)
			for (Price price : prices) {

				// 3. PriceDetail 리스트 순회 (하위 디테일 - 행 생성)
				// PriceDetail 엔티티에 priceDetails 필드가 매핑되어 있어야 합니다.
				for (PriceDetail detail : price.getPriceDetails()) {

					Map<String, Object> row = new HashMap<>();

					// Price 객체에서 단가그룹 정보 가져오기
					row.put("단가그룹코드", price.getPriceGroupCode());
					row.put("단가그룹명", price.getPriceGroupName());
					row.put("단가유형", price.getPriceType());
					row.put("할인율", price.getDiscountPct());
					row.put("단가적용시작일", price.getStartDate());
					row.put("단가적용종료일", price.getEndDate());
					row.put("사용구분", price.getUsageStatus());
					row.put("비고", price.getRemarks());

					// Partner 객체 (PriceDetail을 통해 접근)
					Partner partner = detail.getPartner();

					if (partner != null) {
						// Partner 엔티티에서 이름과 유형 정보 가져오기
						row.put("거래처코드", detail.getPartnerCode()); // PriceDetail에서 FK 코드를 가져올 수도 있습니다.
						row.put("거래처명", partner.getPartnerName());
						row.put("거래처유형", partner.getPartnerType());
					} else {
						// 조인된 Partner 정보가 없는 경우 (LEFT JOIN 사용 시)
						row.put("거래처코드", detail.getPartnerCode());
						row.put("거래처명", null);
						row.put("거래처유형", null);
					}

					// 완성된 행을 rows 리스트에 추가 (PriceDetail 하나당 한 행)
					rows.add(row);
				}
			}
		}

		// 4. 최종 결과 반환
		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	public Map<String, Object> getTableDataFromProducts(List<Price> prices) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!prices.isEmpty()) {
			// 1. 컬럼 정의
			columns.add("품목명");
			columns.add("품목코드");
			columns.add("품목그룹");
			columns.add("단가그룹코드");
			columns.add("단가그룹명");
			columns.add("단가유형");
			columns.add("할인율");
			columns.add("단가적용시작일");
			columns.add("단가적용종료일");
			columns.add("사용구분");
			columns.add("비고");

			// 2. Price 리스트 순회 (상위 그룹)
			for (Price price : prices) {

				// 3. PriceDetail 리스트 순회 (하위 디테일 - 행 생성)
				// PriceDetail 엔티티에 priceDetails 필드가 매핑되어 있어야 합니다.
				for (PriceDetail detail : price.getPriceDetails()) {

					Map<String, Object> row = new HashMap<>();

					// Price 객체에서 단가그룹 정보 가져오기 (PriceDetail의 각 행마다 동일하게 적용)
					row.put("단가그룹코드", price.getPriceGroupCode());
					row.put("단가그룹명", price.getPriceGroupName());
					row.put("단가유형", price.getPriceType());
					row.put("할인율", price.getDiscountPct());
					row.put("단가적용시작일", price.getStartDate());
					row.put("단가적용종료일", price.getEndDate());
					row.put("사용구분", price.getUsageStatus());
					row.put("비고", price.getRemarks());

					// Product 객체 (PriceDetail을 통해 접근)
					Product product = detail.getProduct();

					if (product != null) {
						// Product 엔티티에서 품목 정보 가져오기
						row.put("품목코드", detail.getProductCode()); // PriceDetail에서 FK 코드를 가져올 수도 있습니다.
						row.put("품목명", product.getProductName());
						row.put("품목그룹", product.getProductGroup());
					} else {
						// 조인된 Product 정보가 없는 경우
						row.put("품목코드", detail.getProductCode());
						row.put("품목명", null);
						row.put("품목그룹", null);
					}

			
					rows.add(row);
				} 
			} 
		}

		// 4. 최종 결과 반환
		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	public Map<String, Object> getTableDataFromPrice(List<Price> prices) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!prices.isEmpty()) {
			// 컬럼 정의;
			columns.add("단가그룹코드");
			columns.add("단가그룹명");
			columns.add("단가유형");
			columns.add("할인율");
			columns.add("사용구분");
			columns.add("비고");
			columns.add("단가적용시작일");
			columns.add("단가적용종료일");
			columns.add("거래처설정");
			columns.add("품목설정");

			for (Price price : prices) {
				Map<String, Object> row = new HashMap<>();
				row.put("단가그룹코드", price.getPriceGroupCode());
				row.put("단가그룹명", price.getPriceGroupName());
				row.put("단가유형", price.getPriceType());
				row.put("할인율", price.getDiscountPct());
				row.put("단가적용시작일", price.getStartDate());
				row.put("단가적용종료일", price.getEndDate());
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

	@Override
	public Price getPriceByPriceGroupCode(String PriceGroupCode) {
		return priceRepository.findByPriceGroupCode(PriceGroupCode);
	}

}
