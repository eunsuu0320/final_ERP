package com.yedam.common.domain;

import lombok.Data;

@Data
public class FindPassword {
	private String companyCode;
    private String userId;
    private String email;
    private String recaptcha;
}
