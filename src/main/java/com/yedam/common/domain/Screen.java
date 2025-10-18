package com.yedam.common.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "SCREEN")
@Data
public class Screen {

    @Id
    private String screenCode;
    private String moduleCode;
    private String screenName;
    private String usageStatus;
}