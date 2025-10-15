package com.yedam.common.domain;

import lombok.Data;

@Data
public class RolePermSaveReq {
    private String roleCode;
    private String screenCode;
    private boolean canRead;
    private boolean canCreate;
    private boolean canUpdate;
    private boolean canDelete;
}
