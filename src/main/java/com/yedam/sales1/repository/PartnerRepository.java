package com.yedam.sales1.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.sales1.domain.Invoice;
import com.yedam.sales1.domain.Partner;
import com.yedam.sales1.dto.PartnerModalDto;

@Repository
public interface PartnerRepository extends
		JpaRepository<Partner, String>{

	@Query("SELECT p FROM Partner p "
			+ "WHERE p.companyCode = :companyCode ")
	List<Partner> findAll(@Param("companyCode") String companyCode);
	
	@Query("SELECT MAX(p.partnerCode) FROM Partner p")
	String findMaxPartnerCode();

	Partner findByPartnerCode(String partnerCode);
	
	Partner findByPartnerName(String partnerName);
	

	
	
	@Query("""
		    select new com.yedam.sales1.dto.PartnerModalDto(
		        p.partnerCode,
		        p.partnerName,
		        p.partnerPhone,
		        p.manager,
		        e.name,
		        e.phone,
		        p.postCode,
		        p.address
		    ) 
		    from Partner p
		    JOIN p.managerEmp e
		    where p.companyCode = :companyCode
		""")
		List<PartnerModalDto> findPartnerModalDataByCompanyCode(@Param("companyCode") String companyCode);
	
	
	
	
	
	@Query("SELECT p FROM Partner p "
			+ "WHERE (:#{#searchVo.partnerName} IS NULL OR p.partnerName = :#{#searchVo.partnerName}) "
			+ "AND p.companyCode = :companyCode ")
	List<Partner> findByFilter(@Param("searchVo") Partner searchVo, @Param("companyCode") String companyCode);

}
