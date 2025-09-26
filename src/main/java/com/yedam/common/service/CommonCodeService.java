package com.yedam.common.service;

import java.util.List;
import java.util.Map;

import com.yedam.common.domain.CommonCode;

public interface CommonCodeService {

	List<CommonCode> findByCodeGroup(String codeGroup);

	// 공통코드 그룹으로 가져오기
	public Map<String, List<CommonCode>> getCodes(String godeStrings);
}
