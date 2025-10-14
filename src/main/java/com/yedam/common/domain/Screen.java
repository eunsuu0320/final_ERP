package com.yedam.common.domain;

import org.hibernate.annotations.JoinColumnOrFormula;
import org.hibernate.annotations.JoinColumnsOrFormulas;
import org.hibernate.annotations.JoinFormula;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "SCREEN")
@Data
public class Screen {

    @Id
    private String screenCode;

    private String screenName;
    private String usageStatus;  // 공통코드: GROUP_ID='USE_YN', CODE_ID = usageStatus
    private String moduleCode;   // 공통코드: GROUP_ID='MODULE', CODE_ID = moduleCode

    // === 조인: MODULE 이름 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(
            column = @JoinColumn(name = "moduleCode", referencedColumnName = "codeId", insertable = false, updatable = false)
        ),
        @JoinColumnOrFormula(
            formula = @JoinFormula(value = "'MODULE'", referencedColumnName = "groupId")
        )
    })
    private CommonCode module; // module.codeName 사용 가능

    // === 조인: 사용여부 이름 ===
    @ManyToOne(fetch = FetchType.LAZY)
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumnsOrFormulas({
        @JoinColumnOrFormula(
            column = @JoinColumn(name = "usageStatus", referencedColumnName = "codeId", insertable = false, updatable = false)
        ),
        @JoinColumnOrFormula(
            formula = @JoinFormula(value = "'USE_YN'", referencedColumnName = "groupId")
        )
    })
    private CommonCode usage; // usage.codeName 사용 가능
}
