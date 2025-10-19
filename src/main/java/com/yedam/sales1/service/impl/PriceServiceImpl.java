package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;
import com.yedam.sales1.domain.PriceDetail;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.repository.PriceDetailRepository;
import com.yedam.sales1.repository.PriceRepository;
import com.yedam.sales1.service.PriceService;

@Service
public class PriceServiceImpl implements PriceService {

    private final PriceRepository priceRepository;
    private final PriceDetailRepository priceDetailRepository;

    @Autowired
    public PriceServiceImpl(PriceRepository priceRepository, PriceDetailRepository priceDetailRepository) {
        this.priceRepository = priceRepository;
        this.priceDetailRepository = priceDetailRepository;
    }

    @Override
    public List<Price> getAllPrice() {
    	String companyCode = getCompanyCodeFromAuthentication();
        return priceRepository.findAllWithAllRelations(companyCode);
    }
    
    
    @Override
    public List<Price> getFilterPrice(Price searchVo) {
    	String companyCode = getCompanyCodeFromAuthentication();

        return priceRepository.findByFilter(searchVo, companyCode);
    }

    @Override
    public List<Price> getAllPricePartner() {
    	String companyCode = getCompanyCodeFromAuthentication();

        return priceRepository.findAllWithPartner(companyCode);
    }

    @Override
    public List<Price> getAllPriceProduct() {
    	String companyCode = getCompanyCodeFromAuthentication();

        return priceRepository.findAllWithProduct(companyCode);
    }

    @Override
    public List<String> getAllPartner(Integer priceUniqueCode) {
        return priceRepository.findPartnerCodes(priceUniqueCode);
    }

    @Override
    public Map<String, Object> getProduct(List<String> priceProducts) {
        return Map.of("priceProducts", priceProducts);
    }

    @Override
    public Map<String, Object> getPartner(List<String> pricePartners) {
        return Map.of("pricePartners", pricePartners);
    }

    @Override
    public List<String> getAllProduct(Integer priceUniqueCode) {
        return priceRepository.findProductCodes(priceUniqueCode);
    }

    @Override
    public Map<String, Object> getTableDataFromProductModal(List<Product> products) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!products.isEmpty()) {
            columns.add("품목명");
            columns.add("품목코드");
            columns.add("품목그룹");
            columns.add("규격/단위");

            for (Product product : products) {
                Map<String, Object> row = new HashMap<>();
                row.put("품목명", product.getProductName());
                row.put("품목코드", product.getProductCode());
                row.put("품목그룹", product.getProductGroup());
                row.put("규격/단위", product.getProductSize() + " " + product.getUnit());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    public Map<String, Object> getTableDataFromPartnerModal(List<Partner> partners) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!partners.isEmpty()) {
            columns.add("거래처명");
            columns.add("거래처코드");
            columns.add("거래처유형");
            columns.add("등급");

            for (Partner partner : partners) {
                Map<String, Object> row = new HashMap<>();
                row.put("거래처명", partner.getPartnerName());
                row.put("거래처코드", partner.getPartnerCode());
                row.put("거래처유형", partner.getPartnerType());
                row.put("등급", "vip");
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    public Map<String, Object> getTableDataFromPartners(List<Price> prices) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!prices.isEmpty()) {
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

            for (Price price : prices) {
                for (PriceDetail detail : price.getPriceDetails()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("단가그룹코드", price.getPriceGroupCode());
                    row.put("단가그룹명", price.getPriceGroupName());
                    row.put("단가유형", price.getPriceType());
                    row.put("할인율", (price.getDiscountPct() * 100) + " %");
                    row.put("단가적용시작일", price.getStartDate());
                    row.put("단가적용종료일", price.getEndDate());
                    row.put("사용구분", price.getUsageStatus());
                    row.put("비고", price.getRemarks());

                    Partner partner = detail.getPartner();
                    if (partner != null) {
                        row.put("거래처코드", detail.getPartnerCode());
                        row.put("거래처명", partner.getPartnerName());
                        row.put("거래처유형", partner.getPartnerType());
                    } else {
                        row.put("거래처코드", detail.getPartnerCode());
                        row.put("거래처명", null);
                        row.put("거래처유형", null);
                    }

                    rows.add(row);
                }
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    public Map<String, Object> getTableDataFromProducts(List<Price> prices) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!prices.isEmpty()) {
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

            for (Price price : prices) {
                for (PriceDetail detail : price.getPriceDetails()) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("단가그룹코드", price.getPriceGroupCode());
                    row.put("단가그룹명", price.getPriceGroupName());
                    row.put("단가유형", price.getPriceType());
                    row.put("할인율", (price.getDiscountPct() * 100) + " %");
                    row.put("단가적용시작일", price.getStartDate());
                    row.put("단가적용종료일", price.getEndDate());
                    row.put("사용구분", price.getUsageStatus());
                    row.put("비고", price.getRemarks());

                    Product product = detail.getProduct();
                    if (product != null) {
                        row.put("품목코드", detail.getProductCode());
                        row.put("품목명", product.getProductName());
                        row.put("품목그룹", product.getProductGroup());
                    } else {
                        row.put("품목코드", detail.getProductCode());
                        row.put("품목명", null);
                        row.put("품목그룹", null);
                    }

                    rows.add(row);
                }
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    public Map<String, Object> getTableDataFromPrice(List<Price> prices) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        if (!prices.isEmpty()) {
            columns.add("단가고유코드");
            columns.add("거래처코드");
            columns.add("거래처명");
            columns.add("거래처유형");
            columns.add("품목코드");
            columns.add("품목명");
            columns.add("품목그룹");
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
                row.put("단가고유코드", price.getPriceUniqueCode());
                row.put("단가그룹코드", price.getPriceGroupCode());
                row.put("단가그룹명", price.getPriceGroupName());
                row.put("단가유형", price.getPriceType());
                row.put("할인율", (price.getDiscountPct() * 100) + " %");
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
        price.setPriceUniqueCode(null);

        String newPriceGroupCode = generatePriceGroupCode();
        price.setPriceGroupCode(newPriceGroupCode);

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
    public Price getPriceByPriceGroupCode(String priceGroupCode) {
    	String companyCode = getCompanyCodeFromAuthentication();
    	System.out.println("============priceServiceImpl 확인===============");
    	System.out.println(companyCode);
    	System.out.println(priceGroupCode);
        return priceRepository.findByPriceGroupCode(priceGroupCode, companyCode);
    }

    // =========================================================================
    // PriceDetail 저장 로직
    // =========================================================================

    @Transactional
    @Override
    public PriceDetail savePriceDetailPartner(Integer priceUniqueCode, List<String> partnerCodes) {
        priceDetailRepository.deleteByPriceUniqueCode(priceUniqueCode);

        if (partnerCodes != null && !partnerCodes.isEmpty()) {
            List<String> newCodes = generateNewEstimateCodes(partnerCodes.size());

            List<PriceDetail> newDetails = new ArrayList<>();
            for (int i = 0; i < partnerCodes.size(); i++) {
                PriceDetail detail = new PriceDetail();
                detail.setPriceDetailCode(newCodes.get(i));
                detail.setPriceUniqueCode(priceUniqueCode);
                detail.setPartnerCode(partnerCodes.get(i));
                detail.setCompanyCode(getCompanyCodeFromAuthentication());
                newDetails.add(detail);
            }

            priceDetailRepository.saveAll(newDetails);
        }

        PriceDetail result = new PriceDetail();
        result.setPriceUniqueCode(priceUniqueCode);
        return result;
    }

    @Transactional
    @Override
    public PriceDetail savePriceDetailProduct(Integer priceUniqueCode, List<String> productCodes) {
        priceDetailRepository.deleteByPriceUniqueCode(priceUniqueCode);

        if (productCodes != null && !productCodes.isEmpty()) {
            List<String> newCodes = generateNewEstimateCodes(productCodes.size());

            List<PriceDetail> newDetails = new ArrayList<>();
            for (int i = 0; i < productCodes.size(); i++) {
                PriceDetail detail = new PriceDetail();
                detail.setPriceDetailCode(newCodes.get(i));
                detail.setPriceUniqueCode(priceUniqueCode);
                detail.setProductCode(productCodes.get(i));
                detail.setCompanyCode(getCompanyCodeFromAuthentication());
                newDetails.add(detail);
            }

            priceDetailRepository.saveAll(newDetails);
        }

        PriceDetail result = new PriceDetail();
        result.setPriceUniqueCode(priceUniqueCode);
        return result;
    }

    /** Security 인증 정보에서 회사 코드 추출 */
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

    /**
     * PriceDetailCode를 여러 개 생성
     * @param count 생성할 코드 개수
     * @return 생성된 코드 리스트
     */
    private List<String> generateNewEstimateCodes(int count) {
        String maxCode = priceDetailRepository.findMaxPriceDetailCode();
        int startNum = 1;
        String prefix = "PD";

        if (maxCode != null && maxCode.startsWith(prefix)) {
            try {
                startNum = Integer.parseInt(maxCode.substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                System.out.println(">>> 숫자 변환 실패, 1부터 시작");
            }
        }

        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            codes.add(String.format("%s%04d", prefix, startNum + i));
        }
        return codes;
    }
}
