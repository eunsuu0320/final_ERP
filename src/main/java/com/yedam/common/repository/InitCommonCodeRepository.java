package com.yedam.common.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class InitCommonCodeRepository {

    private final JdbcTemplate jdbcTemplate;

    /** C001을 템플릿으로, 대상 회사에 기본 공통코드 삽입 */
    public void initDefaultsFromC001(String targetCompanyCode) {
        jdbcTemplate.update("BEGIN PROC_INIT_COMMON_CODE_DEFAULTS(?, ?); END;",
                targetCompanyCode, "C001");
    }

    /** 템플릿 회사를 변경해서 쓰고 싶을 때 */
    public void initDefaultsFrom(String targetCompanyCode, String sourceCompanyCode) {
        jdbcTemplate.update("BEGIN PROC_INIT_COMMON_CODE_DEFAULTS(?, ?); END;",
                targetCompanyCode, sourceCompanyCode);
    }
}
