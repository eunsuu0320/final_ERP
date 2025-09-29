package com.yedam.common.domain.payment;

import lombok.Data;

@Data
public class PayRequest {
    private String orderId;
    private String userId;
    private String itemName;
    private int amount;
}
