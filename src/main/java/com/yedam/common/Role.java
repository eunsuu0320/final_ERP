package com.yedam.common;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "ROLE")
@Data
public class Role implements Prefixable {
    
	@Id
	@GeneratedValue(generator = "sequence-id-generator")
    @GenericGenerator(
            name = "sequence-id-generator",
            strategy = "com.yedam.common.SequenceIdGenerator"
    )
    private String roleCode;        // ROLE_CODE (PK)
	
    private String roleName;        // ROLE_NAME
    private String companyCode;     // COMPANY_CODE
    private String remk;            // REMK
    
    @Override
	public String getPrefix() {
		return "ROLE";
	}

	@Override
	public String getSequenceName() {
		return "ROLE_SEQ";
	}
}