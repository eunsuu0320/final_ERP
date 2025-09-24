// src/main/java/com/yedam/ac/service/impl/StatementQueryServiceImpl.java
package com.yedam.ac.service.impl;

import java.util.Collections;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;

import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

@Service
public class StatementQueryServiceImpl implements StatementQueryService {
    @Override
    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
        // TODO: 실제 조회 로직으로 대체 (현재는 앱 기동용 빈 응답)
        return new PageImpl<>(Collections.emptyList());
    }
}
