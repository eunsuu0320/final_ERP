package com.yedam.common.domain.payment;

import lombok.Data;

@Data
public class KakaoApproveResponse {
    private String aid;
    private String tid;
    private String cid;
    private String partner_order_id;
    private String partner_user_id;
    private Amount amount;

    @Data
    public static class Amount {
        private int total;
        private int tax_free;
        private int vat;
    }
}

