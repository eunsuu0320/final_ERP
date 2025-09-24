// com.yedam.ac.web.dto/PartnerLookupDto.java
package com.yedam.ac.web.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartnerLookupDto {
    private String partnerCode;
    private String partnerName;
    private String tel;
    private String picName;
}
