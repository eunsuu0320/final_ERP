package com.yedam.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.yedam.common.domain.SystemUser;

public interface UserRepository extends JpaRepository<SystemUser, String> {
	Optional<SystemUser> findByUserId(String userId);

	Optional<SystemUser> findByUserIdAndCompanyCode(String userId, String companyCode);

	/*
	 * @Query("SELECT u FROM SYSTEMUSER u JOIN EMPLOYEE e ON u.EMP_CODE = e.EMP_CODE "
	 * +
	 * "WHERE u.COMPANY_CODE = :companyCode AND u.USER_ID = :userId AND e.EMAIL = :email"
	 * ) Optional<SystemUser> findByCompanyCodeAndUserIdAndEmail(
	 *
	 * @Param("companyCode") String companyCode,
	 *
	 * @Param("userId") String userId,
	 *
	 * @Param("email") String email );
	 */

	@Query("SELECT u FROM SystemUser u JOIN Employee e ON u.empCode = e.empCode " +
	           "WHERE u.companyCode = :companyCode AND u.userId = :userId AND e.email = :email")
    Optional<SystemUser> findUserWithEmail(@Param("companyCode") String companyCode,
                                           @Param("userId") String userId,
                                           @Param("email") String email);
}
