package com.example.OnlineBankacilik.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineBankacilik.dto.AccountRequestDto;
import com.example.OnlineBankacilik.dto.AccountResponseDto;
import com.example.OnlineBankacilik.dto.TransactionRequestDto;
import com.example.OnlineBankacilik.service.AccountService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	public ResponseEntity<AccountResponseDto> openAnAcoount(@Valid @RequestBody AccountRequestDto dto) {
		return ResponseEntity.status(HttpStatus.CREATED).body(accountService.accountOpen(dto));
	}

	@GetMapping
	public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
		return ResponseEntity.ok(accountService.allAccounts());
	}

	@GetMapping("/{accountNo}")
	public ResponseEntity<AccountResponseDto> getByIdAccount(@PathVariable String accountNo) {
		return ResponseEntity.ok(accountService.getAccount(accountNo));
	}

	@GetMapping("/customer/{customerId}")
	public ResponseEntity<List<AccountResponseDto>> getByIdCustomer(@PathVariable Long customerId) {
		return ResponseEntity.ok(accountService.customerAccounts(customerId));
	}

	@DeleteMapping("/{accountNo}")
	public ResponseEntity<Void> closeAccount(@PathVariable String accountNo) {
		accountService.closeAccount(accountNo);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{accountNo}/deposit")
	public ResponseEntity<AccountResponseDto> deposit(@PathVariable String accountNo,
			@Valid @RequestBody TransactionRequestDto request) {
		return ResponseEntity.ok(accountService.deposit(accountNo, request));
	}

	@PostMapping("/{accountNo}/withdraw")
	public ResponseEntity<AccountResponseDto> withdraw(@PathVariable String accountNo,
			@Valid @RequestBody TransactionRequestDto request) {
		return ResponseEntity.ok(accountService.withdraw(accountNo, request));
	}

	@PostMapping("/{accountNo}/interest")
	public ResponseEntity<AccountResponseDto> processInterest(@PathVariable String accountNo) {
		return ResponseEntity.ok(accountService.processInterest(accountNo));
	}
}
