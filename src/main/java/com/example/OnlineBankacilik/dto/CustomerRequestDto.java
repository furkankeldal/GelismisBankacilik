package com.example.OnlineBankacilik.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRequestDto {

	@NotBlank
	private String nameSurname;
	@NotBlank
	@Pattern(regexp = "^[0-9]{11}$")
	private String tcKimlikNo;
	@NotBlank
	private String number;
	@Email
	@NotBlank
	private String email;
}
