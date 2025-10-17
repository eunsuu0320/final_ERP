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

	// 파일이 저장될 웹 접근 가능한 하위 경로 (DB에 저장될 경로)
	private static final String PRODUCT_IMG_SUB_PATH = File.separator + "erp" + File.separator + "assets"
			+ File.separator + "img" + File.separator + "sales1" + File.separator;

	// 날짜 포맷 패턴 정의
	private static final String DATE_FORMAT_PATTERN = "yyyy/MM/dd";

	@Autowired
	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public List<Product> getAllProduct() {
		return productRepository.findAll();
	}

	@Override
	public List<Product> getFilterProduct(Product searchVo) {
		return productRepository.findByFilter(searchVo);
	}

	@Override
	public Map<String, Object> getTableDataFromProducts(List<Product> products) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_PATTERN);

		if (!products.isEmpty()) {
			// Define column names to be displayed on the screen
			columns.add("품목코드");
			columns.add("품목명");
			columns.add("품목그룹");
			columns.add("규격/단위");
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

				// 날짜 포맷팅 로직 추가 (생성일자)
				Date createDate = product.getCreateDate();
				if (createDate != null) {
					row.put("생성일자", formatter.format(createDate));
				} else {
					row.put("생성일자", null);
				}

				// 날짜 포맷팅 로직 추가 (수정일자)
				Date updateDate = product.getUpdateDate();
				if (updateDate != null) {
					row.put("수정일자", formatter.format(updateDate));
				} else {
					row.put("수정일자", null);
				}

				row.put("비고", product.getRemarks());
				row.put("사용여부", product.getUsageStatus());
				row.put("창고코드", product.getWarehouseCode());
				row.put("재고", product.getStock());
				rows.add(row);
			}
		}

		return Map.of("columns", columns, "rows", rows);
	}

	@Override
	@Transactional
	public Product saveProduct(Product product, MultipartFile multipartfile) {

		// 폼에서 넘어온 객체를 기반으로 신규/수정 판단
		boolean isNewRegistration = (product.getProductCode() == null || product.getProductCode().isEmpty());
		Product productToSave = product;
		Product existingProduct = null;

		// 1. 수정 작업 시 기존 상품 조회 및 데이터 복사 (JPA 영속성 활용)
		if (!isNewRegistration) {
			existingProduct = productRepository.findByProductCode(product.getProductCode());
			if (existingProduct == null) {
				isNewRegistration = true; // DB에 코드가 없으면 신규로 전환
			} else {
				// 수정: 기존 엔티티(existingProduct)를 영속 상태로 사용하여 폼 데이터로 업데이트합니다.
				productToSave = existingProduct;

				// 폼에서 넘어온 업데이트 가능한 필드를 existingProduct에 복사/설정합니다.
				productToSave.setProductName(product.getProductName());
				productToSave.setProductGroup(product.getProductGroup());
				productToSave.setProductSize(product.getProductSize());
				productToSave.setUnit(product.getUnit());
				productToSave.setPrice(product.getPrice());
				productToSave.setRemarks(product.getRemarks());
				productToSave.setWarehouseCode(product.getWarehouseCode());

				// imgPath는 파일 처리 후 최종 결정됩니다.

				// 기존 필드 유지 (CreateDate, CompanyCode 등은 업데이트 로직에서 제외)
				// productToSave (existingProduct)는 이미 기존 필드 값을 가지고 있습니다.
			}
		}

		// 2. 신규 등록 시 고유 정보 설정
		if (isNewRegistration) {
			// 회사코드
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			String companyCode = username != null && username.contains(":") ? username.trim().split(":")[0].trim()
					: username.trim();
			productToSave.setCompanyCode(companyCode);

			String newProductCode = generateProductCode();
			productToSave.setProductCode(newProductCode);

			int min = 100;
			int max = 500;
			int randomStock = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
			productToSave.setStock(randomStock);
			productToSave.setUsageStatus("Y");

			// 신규 등록 시 생성일자 설정 (JPA @PrePersist 또는 여기서 설정)
			productToSave.setCreateDate(new Date());
		}

		// 3. 이미지 처리 로직 (로컬 파일 시스템에 저장)
		try {
			if (multipartfile != null && !multipartfile.isEmpty()) {

				// A. 파일 시스템에 저장될 절대 경로를 Path 객체로 안전하게 구성
				// System.getProperty("user.dir")의 결과 경로를 기반으로 Path 객체를 생성합니다.
				String uploadRoot = System.getProperty("user.dir");

				// Paths.get을 사용하여 OS에 맞는 절대 경로를 구성합니다.
				Path absoluteUploadPath = Paths.get(uploadRoot, PRODUCT_IMG_SUB_PATH);

				// 디버깅 출력: 실제 파일이 저장될 경로를 확인
				System.out.println("DEBUG | 파일 저장 시도 절대 경로: " + absoluteUploadPath.toString());

				// 디렉토리가 없으면 생성
				File dir = absoluteUploadPath.toFile();
				if (!dir.exists()) {
					System.out.println("DEBUG | 디렉토리 생성: " + dir.mkdirs());
					if (!dir.exists()) {
						throw new IOException("파일 저장 디렉토리를 생성할 수 없습니다: " + absoluteUploadPath);
					}
				}

				// B. 고유 파일 이름 생성 (UUID + 원본 확장자)
				String originalFilename = multipartfile.getOriginalFilename();
				String extension = "";
				if (originalFilename != null && originalFilename.lastIndexOf(".") != -1) {
					extension = originalFilename.substring(originalFilename.lastIndexOf("."));
				}

				String savedFileName = UUID.randomUUID().toString().substring(0, 8) + extension;

				// C. 파일 저장 실행
				Path targetLocation = absoluteUploadPath.resolve(savedFileName);
				System.out.println("DEBUG | 최종 파일 저장 위치: " + targetLocation.toString());

				multipartfile.transferTo(targetLocation.toFile());

				// D. DB에 저장될 웹 접근 가능한 상대 경로 설정 (슬래시(/) 사용)
				// DB에는 웹 접근 경로를 저장합니다.
				productToSave.setImgPath(PRODUCT_IMG_SUB_PATH + savedFileName);

			} else if (!isNewRegistration) {
				// ... (기존 이미지 경로 유지 로직)
				if (product.getImgPath() == null || product.getImgPath().isEmpty()) {
					productToSave.setImgPath(existingProduct.getImgPath());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			// 파일 저장 실패 시 로그를 남기고 예외를 던집니다.
			throw new RuntimeException("상품 이미지 저장 중 오류가 발생했습니다. 메시지: " + e.getMessage(), e);
		}

		System.out.println("최종 이미지 경로: " + productToSave.getImgPath());

		// 4. DB에 저장
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

	@Override
	public Product getProductByProductCode(String productCode) {
		return productRepository.findByProductCode(productCode);
	}
}