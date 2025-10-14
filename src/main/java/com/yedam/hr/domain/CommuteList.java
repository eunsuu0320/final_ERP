package com.yedam.hr.domain;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Data;

@Entity
@Data
public class CommuteList {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "COMMUTE_LIST_SEQ")
    @SequenceGenerator(name = "COMMUTE_LIST_SEQ", sequenceName = "COMMUTE_LIST_SEQ", allocationSize = 1)
    private Long comId;

    private String companyCode;
    private String empCode;
    private String place;
    private String comIs;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private Date onTime;

    @Temporal(TemporalType.TIMESTAMP)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private Date offTime;

    private Double workTime;
    private Double nightTime;
    private Double OtTime;
    private Double holidayTime;

    private String note;
}
