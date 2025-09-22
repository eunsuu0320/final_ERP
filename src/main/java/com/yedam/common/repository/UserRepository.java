package com.yedam.common.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yedam.common.domain.SystemUser;

public interface UserRepository extends JpaRepository<SystemUser, String> {
	Optional<SystemUser> findByUserId(String userId);

	Optional<SystemUser> findByUserIdAndCompanyCode(String username, String companyCode);
}
