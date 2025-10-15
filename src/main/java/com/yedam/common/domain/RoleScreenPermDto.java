package com.yedam.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleScreenPermDto {
	private String moduleCode;
    private String screenCode;
    private String screenName;
    private String readYn;   // Y/N
    private String createYn; // Y/N
    private String updateYn; // Y/N
    private String deleteYn; // Y/N
}
