package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.yedam.sales1.domain.Product;

public interface ProductService {
	List<Product> getAllProduct();

	List<Product> getFilterProduct(Product searchVo);

	Map<String, Object> getTableDataFromProducts(List<Product> products);

	Product saveProduct(Product product, MultipartFile multipartfile);

	Product getProductByProductCode(String productCode);

}
