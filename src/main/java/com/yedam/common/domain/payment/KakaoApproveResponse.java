package com.yedam.common.domain.payment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class KakaoApproveResponse {
    private String aid;
    private String tid;
    private String cid;

    @JsonProperty("partner_order_id")
    private String partnerOrderId;

    @JsonProperty("partner_user_id")
    private String partnerUserId;

    private Amount amount;
    
    private String buyerName;

    @Data
    public static class Amount {
        private int total;
        @JsonProperty("tax_free")
        private int taxFree;
        private int vat;
    }
}


