package com.yedam.common.domain;

import java.util.List;

import lombok.Data;

@Data
public class CodeBulkRequest {
    private String groupId;
    private List<Item> create;
    private List<Item> update;
    private List<Long> delete;

    @Data
    public static class Item {
        private Long codeNum;   // update 전용
        private String codeId;
        private String codeName;
        private String useYn;
    }
}
