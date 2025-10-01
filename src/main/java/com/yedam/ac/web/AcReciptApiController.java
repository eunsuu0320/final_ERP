// src/main/java/com/yedam/ac/web/AcReciptApiController.java
package com.yedam.ac.web;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yedam.ac.service.StatementQueryService;
import com.yedam.ac.web.dto.AcReceiptRow;
import com.yedam.ac.web.dto.StatementSearchForm;
import com.yedam.ac.web.dto.UnifiedStatementRow;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AcReciptApiController {

    private final StatementQueryService queryService;

    @GetMapping("/api/acrecipt")
    public List<AcReceiptRow> search(
            @RequestParam(value = "partnerName", required = false) String partnerName,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        StatementSearchForm form = new StatementSearchForm();
        form.setType("SALES");            // 매출만
        form.setKeyword(partnerName);     // 거래처명 LIKE
        form.setFromDate(from);
        form.setToDate(to);
        form.setPage(0);
        form.setSize(1000);

        var page = queryService.search(form);

        var list = page.getContent();
        return IntStream.range(0, list.size())
                .mapToObj(i -> toRow(i, list.get(i)))
                .toList();
    }

    private AcReceiptRow toRow(int idx, UnifiedStatementRow u) {
        long total = nz(u.amountTotal(), 0L);
        long vat   = nz(u.amountVat(),   0L);          // ★ sales_statement.amount_vat
        long supply = (total >= vat) ? (total - vat) : total;

        return AcReceiptRow.builder()
                .id((long) idx + 1)
                .partnerName(nz(u.partnerName(), ""))
                .productName(nz(u.productName(), ""))   // ★ sales.product_name
                .qty(nz(u.salesQty(), 0))               // ★ sales.sales_qty
                .amountSupply(supply)
                .amountVat(vat)
                .amountTotal(total)
                .salesDate(u.voucherDate())
                .build();
    }

    private static String nz(String v, String def){ return v != null ? v : def; }
    private static Long nz(Long v, Long def){ return v != null ? v : def; }
    private static Integer nz(Integer v, Integer def){ return v != null ? v : def; }
}
