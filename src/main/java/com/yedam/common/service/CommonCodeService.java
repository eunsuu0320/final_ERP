package com.yedam.common.service;

import java.util.List;

import com.yedam.common.domain.CommonCode;

public interface CommonCodeService {

	List<CommonCode> findByCodeGroup(String codeGroup);
}
