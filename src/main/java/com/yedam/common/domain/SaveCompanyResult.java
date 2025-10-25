package com.yedam.common.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SaveCompanyResult {
    private final Company company;
    private final boolean newCompany; // true면 이번에 생성된 회사
}