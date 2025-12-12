package com.example.OnlineBankacilik.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineBankacilik.entity.Account;
import com.example.OnlineBankacilik.entity.Customer;

public interface AccountRepository extends JpaRepository<Account, String> {

	List<Account> findByCustomer(Customer customer);
}
