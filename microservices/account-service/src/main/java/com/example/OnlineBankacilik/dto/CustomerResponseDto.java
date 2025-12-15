package com.example.OnlineBankacilik.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerResponseDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long customerId;
	private String nameSurname;
	private String tcKimlikNo;
	private String number;
	private String email;
	private LocalDateTime registrationDate;

}
