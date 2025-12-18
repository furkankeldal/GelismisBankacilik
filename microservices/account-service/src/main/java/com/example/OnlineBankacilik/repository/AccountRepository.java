package com.example.OnlineBankacilik.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.OnlineBankacilik.entity.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

	List<Account> findByCustomerId(Long customerId);
	
	@Query("SELECT MAX(CAST(a.accountNo AS long)) FROM Account a")
	Long findMaxAccountNo();
}
