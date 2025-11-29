package com.example.OnlineBankacilik.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineBankacilik.entity.Account;

public interface ProcessRepository extends JpaRepository<com.example.OnlineBankacilik.entity.Process, Long> {

	List<com.example.OnlineBankacilik.entity.Process> findByTransactionDate(LocalDateTime transactionDate);

	long count();

	List<com.example.OnlineBankacilik.entity.Process> findByAccount(Account account);
}
