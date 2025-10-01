package com.yedam.common.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.common.domain.CommonCode;
import com.yedam.common.repository.CommonCodeRepository;
import com.yedam.common.service.CommonCodeService;

@Service
public class CommonCodeServiceImpl implements CommonCodeService {

	@Autowired CommonCodeRepository commonCodeRepository;

	@Override
	public List<CommonCode> findByGroupId(String codeGroup) {
		return commonCodeRepository.findByGroupId(codeGroup);
	}

	@Override
	public Map<String, List<CommonCode>> getCodes(String str) {
		String[] godeStrings = str.split(",");

		Map<String, List<CommonCode>> map = new HashMap<String, List<CommonCode>>();

		for (String code : godeStrings) {
			map.put(code, commonCodeRepository.findByGroupId(code));
		}
		return map;
	}
}
