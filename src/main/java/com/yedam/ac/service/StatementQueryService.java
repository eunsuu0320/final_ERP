// src/main/java/com/yedam/ac/service/StatementQueryService.java
package com.yedam.ac.service;

import org.springframework.data.domain.Page;

import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

public interface StatementQueryService {
    Page<UnifiedStatementRow> search(StatementSearchForm form);
}
