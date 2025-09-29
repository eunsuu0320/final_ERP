package com.yedam.hr.domain;

import java.util.Date;

import org.hibernate.annotations.CreationTimestamp;
import org.springframework.format.annotation.DateTimeFormat;

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
public class HrHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "history_seq")
	@SequenceGenerator(name = "history_seq", sequenceName = "SEQ_HISTORY_ID", allocationSize = 1)
	private Long historyId;

	private String companyCode;
	private String empNo;
	private String eventType;
	private String eventDetail;
	private String manager;

	@CreationTimestamp
	@Temporal(TemporalType.TIMESTAMP)
	@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	private Date createdAt;
}
