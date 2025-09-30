//// src/main/java/com/yedam/ac/web/VoucherNoController.java
//package com.yedam.ac.web;
//
//import java.time.LocalDate;
//
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.yedam.ac.service.VoucherNoService;
//import com.yedam.ac.util.CompanyContext;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//public class VoucherNoController {
//
//    private final VoucherNoService voucherNoService;
//    private final CompanyContext companyCtx;
//
//    @GetMapping("/api/vouchers/next")
//    public NextVoucherNoRes next(
//        @RequestParam("type") String type,
//        @RequestParam(value = "date", required = false)
//        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
//    ){
//        String cc = companyCtx.getRequiredCompanyCode();
//        String next = voucherNoService.next(type, date, cc);
//        return new NextVoucherNoRes(type.toUpperCase(), next);
//    }
//
//    public record NextVoucherNoRes(String type, String voucherNo) {}
//}
