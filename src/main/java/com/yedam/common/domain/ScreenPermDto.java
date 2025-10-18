package com.yedam.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ScreenPermDto {
    private String moduleCode;
    private String screenCode;
    private String screenName;
    private String readYn;    // 'Y'/'N'
    private String createYn;  // 'Y'/'N'
    private String updateYn;  // 'Y'/'N'
    private String deleteYn;  // 'Y'/'N'
}
