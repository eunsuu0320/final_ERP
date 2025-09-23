package com.yedam.sales1.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.yedam.sales1.domain.Product;
import com.yedam.sales1.repository.ProductRepository;
import com.yedam.sales1.service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	private final ProductRepository productRepository;

	@Autowired
	public ProductServiceImpl(ProductRepository itemRepository) {
		this.productRepository = itemRepository;
	}

	@Override
	public List<Product> getAllProduct() {
		return productRepository.findAll();
	}

	@Override
	public Map<String, Object> getTableDataFromProducts(List<Product> products) {
		List<Map<String, Object>> rows = new ArrayList<>();
		List<String> columns = new ArrayList<>();

		if (!products.isEmpty()) {
		    // 컬럼명 정의 (화면에 표시할 이름)
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
		        row.put("품목규격", product.getProductSize());
		        row.put("규격/단위", product.getProductSize()+" "+product.getUnit());
		        row.put("이미지", product.getImgPath());
		        row.put("생성일자", product.getCreateDate());
		        row.put("수정일자", product.getUpdateDate());
		        row.put("비고", product.getRemarks());
		        row.put("사용여부", product.getUsageStatus());
		        row.put("창고코드", product.getWarehouseCode());
		        row.put("재고", product.getStock()); // stock 필드 사용
		        rows.add(row);
		    }
		}


		return Map.of("columns", columns, "rows", rows);
	}

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }
}
