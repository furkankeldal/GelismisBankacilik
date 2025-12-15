package com.example.OnlineBankacilik.entity;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;


@Entity
@Getter
@Setter
@Table(name = "customers")
public class Customer {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "name_surname", length = 100, nullable = false)
	private String nameSurname;
	
	@Column(name = "tc_kimlik_no", unique = true, length = 11, nullable = false)
	private String tcKimlikNo;
	
	@Column(name = "phone_number", length = 20)
	private String number;
	
	@Column(name = "email", length = 100, unique = true)
	private String email;
	
	@Column(name = "registration_date", nullable = false, updatable = false)
	private LocalDateTime registrationDate;

	
	@PrePersist
	protected void onCreate() {
		if (registrationDate == null) {
			registrationDate = LocalDateTime.now();
		}
	}
}

