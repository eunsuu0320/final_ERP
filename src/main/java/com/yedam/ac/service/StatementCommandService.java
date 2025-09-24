//// src/main/java/com/yedam/ac/service/StatementCommandService.java
//package com.yedam.ac.service;
//
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import com.yedam.ac.repository.BuyStatementRepository;
//import com.yedam.ac.repository.SalesStatementRepository;
//import com.yedam.ac.repository.StatementRepository;
//
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class StatementCommandService {
//
//    private final SalesStatementRepository salesRepo;
//    private final BuyStatementRepository   buyRepo;
//    private final StatementRepository      stmtRepo;   // ★ 추가 주입
//
////    public Long save(StatementCreateRequest req) {
////        Long voucherNo;
////
////        if ("SALES".equalsIgnoreCase(req.getType())) {
////            SalesStatement e = new SalesStatement();
////            e.setVoucherDate(req.getVoucherDate());
////            e.setSalesCode(req.getSaleCode());
////            e.setPartnerName(req.getPartnerName());
////            e.setEmployee(req.getEmpName());
////            e.setTaxCode(req.getTaxType());
////            e.setAmountSupply(req.getSupply());
////            e.setAmountVat(req.getVat());
////            e.setAmountTotal(req.getTotal());
////            e.setRemark(req.getRemark());
////
////            SalesStatement saved = salesRepo.save(e);
////            voucherNo = saved.getVoucherNo();
////
////        } else if ("BUY".equalsIgnoreCase(req.getType())) {
////            BuyStatement e = new BuyStatement();
////            e.setVoucherDate(req.getVoucherDate());
////            e.setBuyCode(req.getSaleCode());
////            e.setPartnerName(req.getPartnerName());
////            e.setEmployee(req.getEmpName());
////            e.setTaxCode(req.getTaxType());
////            e.setAmountSupply(req.getSupply());
////            e.setAmountVat(req.getVat());
////            e.setAmountTotal(req.getTotal());
////            e.setRemark(req.getRemark());
////
////            BuyStatement saved = buyRepo.save(e);
////            voucherNo = saved.getVoucherNo();
////
////        } else {
////            throw new IllegalArgumentException("type은 SALES 또는 BUY만 지원");
////        }
////
////        // ★ STATEMENT 테이블에도 헤더 한 줄 저장 (NOT NULL 컬럼들 값 채우기)
////        Statement st = new Statement();
////        st.setVoucherNo(voucherNo);
////        st.setCompanyCode("C001");                  // TODO: 회사 코드 실제 값으로 교체
////        st.setVoucherTypeCode(req.getType());       // "SALES" or "BUY" (스키마 코드체계에 맞게 필요시 매핑)
////        st.setVoucherStatusCode("NORMAL");          // TODO: 스키마 정의에 맞는 기본 상태값(예: "N", "NORMAL" 등)
////
////        stmtRepo.save(st);
////
////        return voucherNo;
////    }
//}
//
