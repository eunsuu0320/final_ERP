package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Price;
import com.yedam.sales1.domain.PriceDetail;
import com.yedam.sales1.domain.Product;

public interface PriceService {
	List<Price> getAllPrice();
	
	List<Price> getFilterPrice(Price searchVo);

	
	List<Price> getAllPricePartner();

	List<Price> getAllPriceProduct();

	List<String> getAllPartner(Integer priceUniqueCode);
	
	List<String> getAllProduct(Integer priceUniqueCode);

	Map<String, Object> getTableDataFromPrice(List<Price> price);

	Map<String, Object> getTableDataFromPartners(List<Price> price);

	Map<String, Object> getTableDataFromProducts(List<Price> price);

	Map<String, Object> getTableDataFromPartnerModal(List<Partner> partners);

	Map<String, Object> getPartner(List<String> pricePartners);

	Map<String, Object> getTableDataFromProductModal(List<Product> products);

	Map<String, Object> getProduct(List<String> priceProducts);

	Price savePrice(Price price);

	Price getPriceByPriceGroupCode(String keyword);
	
	PriceDetail savePriceDetailPartner(Integer priceUniqueCode, List<String> partnerCodes);
	
	PriceDetail savePriceDetailProduct(Integer priceUniqueCode, List<String> productCodes);
}
