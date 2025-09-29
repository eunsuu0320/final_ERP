package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Price;

public interface PriceService {
	List<Price> getAllPrice();

	Map<String, Object> getTableDataFromPrice(List<Price> price);
	
	Price savePrice(Price price);

	Price getPriceByPriceGroupCode(String keyword);
}
