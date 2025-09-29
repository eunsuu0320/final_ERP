// src/main/java/com/yedam/ac/domain/BaseCompanyEntity.java
package com.yedam.ac.domain;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@MappedSuperclass
@FilterDef(name = "companyFilter", parameters = @ParamDef(name = "cc", type = String.class))
@Filter(name = "companyFilter", condition = "COMPANY_CODE = :cc")
public abstract class BaseCompanyEntity {

    @Column(name = "COMPANY_CODE", length = 20, nullable = false)
    private String companyCode;
}
