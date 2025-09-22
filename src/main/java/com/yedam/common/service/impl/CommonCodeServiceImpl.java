package com.yedam.common.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.yedam.common.domain.CommonCode;
import com.yedam.common.repository.CommonCodeRepository;
import com.yedam.common.service.CommonCodeService;

@Service
public class CommonCodeServiceImpl implements CommonCodeService {

	@Autowired CommonCodeRepository commonCodeRepository;

	@Override
	public List<CommonCode> findByCodeGroup(String codeGroup) {
		return commonCodeRepository.findByCodeGroup(codeGroup);
	}
}
