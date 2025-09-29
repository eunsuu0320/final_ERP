// src/main/java/com/yedam/ac/util/CompanyContext.java
package com.yedam.ac.util;

public interface CompanyContext {
    /** 세션/헤더/파라미터에서 읽을 세션 키 */
    String ATTR = "COMPANY_CODE";

    /** 없으면 null */
    String getCompanyCode();

    /** 없으면 명확한 예외 */
    default String getRequiredCompanyCode() {
        String cc = getCompanyCode();
        if (cc == null || cc.isBlank()) {
            throw new IllegalStateException("회사코드 누락");
        }
        return cc;
    }
}
