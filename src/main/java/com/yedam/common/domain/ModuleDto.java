// com.yedam.common.dto.ModuleDto
package com.yedam.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ModuleDto {
    private String code; // moduleCode
    private String name; // 공통코드명(없으면 code 그대로)
}
