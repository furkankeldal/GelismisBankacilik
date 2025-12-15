package com.example.OnlineBankacilik.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.OnlineBankacilik.entity.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

}

