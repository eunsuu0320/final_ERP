package com.yedam.common.domain;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import com.yedam.common.Prefixable;
import com.yedam.hr.domain.Employee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "SYSTEM_USER")
@Data
public class SystemUser implements Prefixable {

	@Id
	@GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
	private String userCode;

	private String companyCode;
	private String roleCode;

	@Column(name = "EMP_CODE")
	private String empCode;

	private String userId;
	private String userPw;
	private Date createdDate;
	private String usageStatus;
	private String remk;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EMP_CODE", insertable = false, updatable = false)
    private Employee employee;

	@Override
	public String getPrefix() {
		return "U";
	}

	@Override
	public String getSequenceName() {
		return "SYSTEM_USER_SEQ";
	}
}
