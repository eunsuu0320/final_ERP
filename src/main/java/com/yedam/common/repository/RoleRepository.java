package com.yedam.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.Role;

@Repository
public interface RoleRepository extends JpaRepository<Role, String> {

    @Query(value = """
        SELECT COUNT(1)
          FROM ROLE
         WHERE COMPANY_CODE = :companyCode
           AND UPPER(ROLE_NAME) = UPPER(:roleName)
        """, nativeQuery = true)
    long countByCompanyCodeAndRoleNameCI(@Param("companyCode") String companyCode,
                                         @Param("roleName") String roleName);

    // 1건 조회: ROWNUM = 1
    @Query(value = """
        SELECT *
          FROM ROLE
         WHERE COMPANY_CODE = :companyCode
           AND UPPER(ROLE_NAME) = UPPER(:roleName)
           AND ROWNUM = 1
        """, nativeQuery = true)
    Optional<Role> findAnyByCompanyCodeAndRoleNameCI(@Param("companyCode") String companyCode,
                                                     @Param("roleName") String roleName);

    // 목록 조회
    List<Role> findByCompanyCodeOrderByRoleNameAsc(String companyCode);
    
    long countByCompanyCodeAndRoleCode(String companyCode, String roleCode);
    
    Optional<Role> findByCompanyCodeAndRoleCode(String companyCode, String roleCode);
    
    Optional<Role> findByCompanyCodeAndRoleNameIgnoreCase(String companyCode, String roleName);
    
    Optional<Role> findByCompanyCodeAndRoleName(String companyCode, String roleName);
}
