// src/main/java/com/yedam/ac/repository/AcPartnerRepository.java
package com.yedam.ac.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.ac.domain.AcPartner;

public interface AcPartnerRepository extends JpaRepository<AcPartner, String> {

    /** 회사코드별 상위 200건 (이름순) */
    @Query("select p from AcPartner p where p.companyCode = :cc order by p.partnerName asc")
    List<AcPartner> findTopByCompanyOrderByName(@Param("cc") String companyCode, Pageable pageable);

    /** 회사코드 + 키워드(이름/코드/전화/담당자) OR 검색, 상위 200건 (이름순) */
    @Query("""
        select p from AcPartner p
        where p.companyCode = :cc and (
              lower(p.partnerName) like lower(concat('%', :q, '%'))
           or lower(p.partnerCode) like lower(concat('%', :q, '%'))
           or lower(p.partnerPhone) like lower(concat('%', :q, '%'))
           or lower(p.manager)     like lower(concat('%', :q, '%'))
        )
        order by p.partnerName asc
        """)
    List<AcPartner> searchTopByCompanyAndKeyword(@Param("cc") String companyCode,
                                                 @Param("q")  String keyword,
                                                 Pageable pageable);
}
