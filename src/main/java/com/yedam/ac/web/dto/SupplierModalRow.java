package com.yedam.ac.web.dto;

import com.yedam.ac.domain.Supplier;

public record SupplierModalRow(
        String partnerCode,
        String partnerName,
        String tel,
        String picName // 공급사에 담당자 컬럼이 없으니 빈 값으로
) {
    public static SupplierModalRow of(Supplier s) {
        return new SupplierModalRow(
                s.getSupplierCode(),
                s.getSupplierName(),
                s.getPhone(),
                "" // 필요하면 컬럼 추가 후 매핑
        );
    }
}
