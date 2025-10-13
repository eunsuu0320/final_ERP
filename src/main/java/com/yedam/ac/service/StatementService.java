// src/main/java/com/yedam/ac/service/StatementService.java
package com.yedam.ac.service;

import com.yedam.ac.web.dto.BuyCreateReq;
import com.yedam.ac.web.dto.MoneyReq;
import com.yedam.ac.web.dto.PaymentReq;
import com.yedam.ac.web.dto.SalesCreateReq;
import com.yedam.ac.web.dto.StatementCreateRes;

public interface StatementService {
    StatementCreateRes createSalesStatement(SalesCreateReq req);
    StatementCreateRes createBuyStatement(BuyCreateReq req);
    StatementCreateRes createMoneyStatement(MoneyReq req);
    StatementCreateRes createPaymentStatement(PaymentReq req);
}
