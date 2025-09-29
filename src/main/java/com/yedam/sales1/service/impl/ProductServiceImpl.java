package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	@Autowired
	public ProductServiceImpl(ProductRepository productRepository) {
		this.productRepository = productRepository;
	}

	@Override
	public List<Product> getAllProduct() {
		return productRepository.findAll();
	}

	@Override
	public List<Product> getFilterProduct(String productName, String productGroup, String warehouseCode) {
		return productRepository.findByFilter(productName, productGroup, warehouseCode);
	}

	@Override
	public Map<String, Object> getTableDataFromProducts(List<Product> products) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!products.isEmpty()) {
			// Define column names to be displayed on the screen
			columns.add("품목코드");
			columns.add("품목명");
			columns.add("품목그룹");
			columns.add("규격/단위"); // This combined field is a good idea.
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
				row.put("생성일자", product.getCreateDate());
				row.put("수정일자", product.getUpdateDate());
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
		boolean isNewRegistration = (product.getProductCode() == null || product.getProductCode().isEmpty());

		Product existingProduct = null;
		if (!isNewRegistration) {
			existingProduct = productRepository.findByProductCode(product.getProductCode());
			if (existingProduct == null) {
				isNewRegistration = true;
			}
		}

		if (isNewRegistration) {
			// 회사코드
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			String username = authentication.getName();
			String companyCode = username != null && username.contains(":") ? username.trim().split(":")[0].trim()
					: username.trim();
			product.setCompanyCode(companyCode);

			String newProductCode = generateProductCode();
			product.setProductCode(newProductCode);

			int min = 100;
			int max = 500;
			int randomStock = (int) (Math.floor(Math.random() * (max - min + 1)) + min);
			product.setStock(randomStock);
			product.setUsageStatus("Y");


		} else {
			if (existingProduct.getCreateDate() != null) {
				product.setCreateDate(existingProduct.getCreateDate());
			}

			product.setCompanyCode(existingProduct.getCompanyCode());

			product.setStock(existingProduct.getStock());
			product.setUsageStatus(existingProduct.getUsageStatus());

			if (existingProduct.getCreateDate() != null) {
				product.setCreateDate(existingProduct.getCreateDate());
			}

		}
		
		// 이미지 처리
		if (multipartfile != null && !multipartfile.isEmpty()) {
		} else if (!isNewRegistration && (product.getImgPath() == null || product.getImgPath().isEmpty())) {
		} else if (!isNewRegistration) {
			if (product.getImgPath() == null && existingProduct != null) {
				product.setImgPath(existingProduct.getImgPath());
			}
		}

		System.out.println("이미지는 : " + product.getImgPath());

		return productRepository.save(product);
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