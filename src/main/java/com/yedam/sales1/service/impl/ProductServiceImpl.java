package com.yedam.sales1.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.yedam.sales1.domain.Product;
import com.yedam.sales1.repository.ProductRepository;
import com.yedam.sales1.service.ProductService;

import jakarta.transaction.Transactional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    // DB에 저장될 웹 접근용 경로 (브라우저에서 접근 가능한 경로)
    private static final String PRODUCT_IMG_WEB_PATH = "/uploads/sales1/productImg/";

    // 실제 파일 저장 경로 (서버 내부)
    private static final String PRODUCT_IMG_SAVE_PATH = "uploads/sales1/productImg/";

    // 날짜 포맷 패턴 정의
    private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProduct() {
    	String companyCode = getCompanyCodeFromAuthentication();
        return productRepository.findAll(companyCode);
    }

    @Override
    public List<Product> getFilterProduct(Product searchVo) {
    	String companyCode = getCompanyCodeFromAuthentication();
        return productRepository.findByFilter(searchVo, companyCode);
    }

    @Override
    public Map<String, Object> getTableDataFromProducts(List<Product> products) {
        List<Map<String, Object>> rows = new ArrayList<>();
        List<String> columns = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);

        if (!products.isEmpty()) {
            // 화면에 표시할 컬럼 정의
            columns.add("품목코드");
            columns.add("품목명");
            columns.add("품목그룹");
            columns.add("규격/단위");
            columns.add("입고단가");
            columns.add("출고단가");
            columns.add("이미지");
            columns.add("창고코드");
            columns.add("재고");
            columns.add("생성일자");
            columns.add("수정일자");
            columns.add("사용여부");
            columns.add("비고");

            for (Product product : products) {
                Map<String, Object> row = new HashMap<>();
                row.put("품목코드", product.getProductCode());
                row.put("품목명", product.getProductName());
                row.put("품목그룹", product.getProductGroup());
                row.put("규격/단위", product.getProductSize() + " " + product.getUnit());
                row.put("이미지", product.getImgPath());

                // 날짜 포맷팅 (생성일자)
                Date createDate = product.getCreateDate();
                if (createDate != null) {
                    row.put("생성일자", formatter.format(createDate));
                } else {
                    row.put("생성일자", null);
                }

                // 날짜 포맷팅 (수정일자)
                Date updateDate = product.getUpdateDate();
                if (updateDate != null) {
                    row.put("수정일자", formatter.format(updateDate));
                } else {
                    row.put("수정일자", null);
                }

                row.put("비고", product.getRemarks());
                row.put("사용여부", product.getUsageStatus());
                row.put("창고코드", product.getWarehouseCode());
                row.put("입고단가", product.getInPrice());
                row.put("출고단가", product.getOutPrice());
                row.put("재고", product.getStock());
                rows.add(row);
            }
        }

        return Map.of("columns", columns, "rows", rows);
    }

    @Override
    @Transactional
    public Product saveProduct(Product product, MultipartFile multipartfile) {

        // 신규/수정 여부 판단
        boolean isNewRegistration = (product.getProductCode() == null || product.getProductCode().isEmpty());
        Product productToSave = product;
        Product existingProduct = null;

        // 1. 수정 작업 시 기존 상품 조회
        if (!isNewRegistration) {
            existingProduct = productRepository.findByProductCode(product.getProductCode());
            if (existingProduct == null) {
                isNewRegistration = true; // DB에 코드가 없으면 신규로 전환
            } else {
                // 기존 데이터 복사 및 업데이트
                productToSave = existingProduct;
                productToSave.setProductName(product.getProductName());
                productToSave.setProductGroup(product.getProductGroup());
                productToSave.setProductSize(product.getProductSize());
                productToSave.setUnit(product.getUnit());
                productToSave.setInPrice(product.getInPrice());
                productToSave.setOutPrice(product.getOutPrice());
                productToSave.setRemarks(product.getRemarks());
                productToSave.setWarehouseCode(product.getWarehouseCode());
            }
        }

        // 2. 신규 등록 시 기본값 세팅
        if (isNewRegistration) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            String companyCode = username != null && username.contains(":")
                    ? username.trim().split(":")[0].trim()
                    : username.trim();
            productToSave.setCompanyCode(companyCode);

            String newProductCode = generateProductCode();
            productToSave.setProductCode(newProductCode);

            int min = 100;
            int max = 500;
            int randomStock = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
            productToSave.setStock(randomStock);
            productToSave.setUsageStatus("Y");
            productToSave.setCreateDate(new Date());
        }

        // 3. 이미지 처리 로직
        try {
            if (multipartfile != null && !multipartfile.isEmpty()) {

                // 실제 파일이 저장될 절대경로
                String uploadRoot = System.getProperty("user.dir");
                Path absoluteUploadPath = Paths.get(uploadRoot, PRODUCT_IMG_SAVE_PATH);

                System.out.println("DEBUG | 실제 파일 저장 절대경로: " + absoluteUploadPath.toString());

                // 디렉토리가 없으면 생성
                File dir = absoluteUploadPath.toFile();
                if (!dir.exists()) {
                    System.out.println("DEBUG | 디렉토리 생성 결과: " + dir.mkdirs());
                    if (!dir.exists()) {
                        throw new IOException("파일 저장 디렉토리를 생성할 수 없습니다: " + absoluteUploadPath);
                    }
                }

                // 고유 파일명 생성
                String originalFilename = multipartfile.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                String savedFileName = UUID.randomUUID().toString().substring(0, 8) + extension;

                // 파일 저장
                Path targetLocation = absoluteUploadPath.resolve(savedFileName);
                multipartfile.transferTo(targetLocation.toFile());

                // DB에 저장될 웹 경로
                String dbPath = PRODUCT_IMG_WEB_PATH + savedFileName;
                productToSave.setImgPath(dbPath);

            } else if (!isNewRegistration) {
                // 기존 이미지 유지
                if (product.getImgPath() == null || product.getImgPath().isEmpty()) {
                    productToSave.setImgPath(existingProduct.getImgPath());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("상품 이미지 저장 중 오류가 발생했습니다. 메시지: " + e.getMessage(), e);
        }

        System.out.println("최종 이미지 경로(DB): " + productToSave.getImgPath());

        // 4. DB 저장
        return productRepository.save(productToSave);
    }

    private String generateProductCode() {
        String productCode = productRepository.findMaxProductCode();

        int nextNumber = 1;
        if (productCode != null) {
            String productNumber = productCode.replaceAll("\\D", "");
            nextNumber = Integer.parseInt(productNumber) + 1;
        }

        return String.format("P%04d", nextNumber);
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
    

    @Override
    public Product getProductByProductCode(String productCode) {
        return productRepository.findByProductCode(productCode);
    }
}
