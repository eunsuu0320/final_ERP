package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Orders;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.domain.Product;
import com.yedam.sales1.dto.PartnerRegistrationDTO;

public interface PartnerService {
	List<Partner> getAllPartner();
	
	List<Partner> getFilterPartner(Partner searchVo);


	Map<String, Object> getTableDataFromPartners(List<Partner> partners);
	
	Partner savePartner(Partner partner);

	Partner getPartnerByPartnerCode(String keyword);

	Partner saveFullPartnerData(PartnerRegistrationDTO partnerData);
}
