package com.yedam.common.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.common.domain.SystemUser;

public interface UserRepository extends JpaRepository<SystemUser, String> {
	Optional<SystemUser> findByUserId(String userId);

	Optional<SystemUser> findByUserIdAndCompanyCode(String userId, String companyCode);

	@Query("SELECT u FROM SystemUser u JOIN Employee e ON u.empCode = e.empCode " +
	           "WHERE u.companyCode = :companyCode AND u.userId = :userId AND e.email = :email")
    Optional<SystemUser> findUserWithEmail(@Param("companyCode") String companyCode,
                                           @Param("userId") String userId,
                                           @Param("email") String email);
	
	
	@EntityGraph(attributePaths = {"employee", "employee.deptCode"})
	@Query("""
	        select u
	          from SystemUser u
	          left join u.employee e
	         where u.companyCode = :companyCode
	           and (:dept  is null or :dept  = '' or e.dept         = :dept)
	           and (:useYn is null or :useYn = '' or u.usageStatus  = :useYn)
	           and (
	                :kw is null or :kw = '' or
	                lower(u.empCode) like lower(concat('%', :kw, '%')) or
	                lower(u.userId)  like lower(concat('%', :kw, '%')) or
	                lower(e.name)    like lower(concat('%', :kw, '%'))
	           )
	         order by e.dept, e.name
	    """)
    List<SystemUser> findUserByCompanyCode(@Param("companyCode") String companyCode,
                                           @Param("kw") String kw,
                                           @Param("dept") String dept,
                                           @Param("useYn") String useYn);
	
	
	@EntityGraph(attributePaths = {"employee", "employee.deptCode"})
	Optional<SystemUser> findByUserCode(String userCode);

}
