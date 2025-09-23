package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Product;

public interface ProductService {
	List<Product> getAllProduct();
	
	Map<String, Object> getTableDataFromProducts(List<Product> products);

	Product saveProduct(Product product);
}
