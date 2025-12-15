package com.example.OnlineBankacilik.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessRepository extends JpaRepository<com.example.OnlineBankacilik.entity.Process, Long> {

	long count();

	List<com.example.OnlineBankacilik.entity.Process> findByAccountNo(String accountNo);
}

