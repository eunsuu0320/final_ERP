package com.yedam.service;

import java.util.List;
import java.util.Map;

import com.yedam.domain.Product;

public interface ProductService {
	List<Product> getAllProduct();
	
	Map<String, Object> getTableDataFromProducts(List<Product> products);
}
