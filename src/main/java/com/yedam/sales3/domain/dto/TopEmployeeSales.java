// src/main/java/com/yedam/sales3/domain/dto/TopEmployeeSales.java
package com.yedam.sales3.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data @AllArgsConstructor
public class TopEmployeeSales {
    private int rank;
    private String employeeName; // EMPLOYEE.NAME
    private long sales;          // DMND_AMT 합계
}
