package com.yedam.common.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.yedam.common.domain.RolePermission;
import com.yedam.common.domain.RoleScreenPermDto;

@Repository
public interface RolePermissionRepository extends JpaRepository<RolePermission, String> {
	
	@Query("""
	        select new com.yedam.common.domain.RoleScreenPermDto(
	            s.moduleCode, s.screenCode, s.screenName,
	            coalesce(p.readRole,   'N'),
	            coalesce(p.createRole, 'N'),
	            coalesce(p.updateRole, 'N'),
	            coalesce(p.deleteRole, 'N')
	        )
	        from Screen s
	        left join RolePermission p
	               on p.screenCode = s.screenCode
	              and p.roleCode   = :roleCode
	        where (:onlyY = false or s.usageStatus = 'Y')
	          and exists (
	              select 1 from Role r
	               where r.roleCode    = :roleCode
	                 and r.companyCode = :companyCode
	          )
	        order by s.moduleCode, s.screenName
	    """)
	    List<RoleScreenPermDto> findScreensWithPermByRole(
	        @Param("companyCode") String companyCode,
	        @Param("roleCode") String roleCode,
	        @Param("onlyY") boolean onlyY
	    );
	
	// 현재 역할에 대한 모든 권한 로우 한번에 가져와 맵으로 쓰기
    List<RolePermission> findByRoleCode(String roleCode);

    // 존재 확인이나 조회시 char 공백 문제를 피하려면 trim 비교도 가능(선택)
    @Query("""
        select p from RolePermission p
         where trim(p.roleCode) = trim(:roleCode)
           and trim(p.screenCode) = trim(:screenCode)
    """)
    Optional<RolePermission> findOneForUpdate(@Param("roleCode") String roleCode,
                                              @Param("screenCode") String screenCode);
    
    Optional<RolePermission> findByRoleCodeAndScreenCode(String roleCode, String screenCode);
}
