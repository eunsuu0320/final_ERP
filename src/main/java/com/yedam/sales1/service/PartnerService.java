package com.yedam.sales1.service;

import java.util.List;
import java.util.Map;

import com.yedam.sales1.domain.Partner;

public interface PartnerService {
	List<Partner> getAllPartner();

	Map<String, Object> getTableDataFromPartners(List<Partner> partners);
	
	Partner savePartner(Partner partner);

	Partner getPartnerByPartnerCode(String keyword);
}
