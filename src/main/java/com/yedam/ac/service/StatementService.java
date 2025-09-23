package com.yedam.ac.service;

import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

public interface StatementService {
    StatementCreateRes createSalesStatement(SalesCreateReq req);
    StatementCreateRes createBuyStatement(BuyCreateReq req);
}
