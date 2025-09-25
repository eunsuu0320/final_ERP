//// com.yedam.ac.web.AcStatementApiController.java
//package com.yedam.ac.web;
//
//import org.springframework.data.domain.Page;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.yedam.ac.service.StatementCommandService;
//import com.yedam.ac.service.StatementQueryService;
//import com.yedam.ac.web.dto.StatementCreateRequest;
//import com.yedam.ac.web.dto.StatementSearchForm;
//import com.yedam.ac.web.dto.UnifiedStatementRow;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequiredArgsConstructor
//public class AcStatementApiController {
//
//    private final StatementQueryService   queryService;
//    private final StatementCommandService commandService;
//
//    @GetMapping("/api/statements")
//    public Page<UnifiedStatementRow> search(StatementSearchForm form) {
//        return queryService.search(form);
//    }
//
//    @PostMapping("/api/statements")
//    public ResponseEntity<?> create(@RequestBody StatementCreateRequest req) {
//        Long newNo = commandService.save(req);
//
//        String formatted = (req.getVoucherDate() != null)
//                ? req.getVoucherDate().getYear() + "-" + newNo
//                : String.valueOf(newNo);
//
//        return ResponseEntity.ok(java.util.Map.of(
//            "result", "ok",
//            "type", req.getType(),
//            "voucherNo", formatted
//        ));
//    }
//}
