package com.example.OnlineBankacilik.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineBankacilik.dto.ProcessRequestDto;
import com.example.OnlineBankacilik.dto.ProcessResponseDto;
import com.example.OnlineBankacilik.service.ProcessService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/process")
public class ProcessController {

	@Autowired
	private ProcessService processService;

	@PostMapping("/deposit-money")
	public ResponseEntity<ProcessResponseDto> depositMoney(@Valid @RequestBody ProcessRequestDto dto) {
		return ResponseEntity.ok(processService.deposit(dto));

	}

	@PostMapping("/withdraw-money")
	public ResponseEntity<ProcessResponseDto> withdrawMoney(@Valid @RequestBody ProcessRequestDto dto) {
		return ResponseEntity.ok(processService.withdraw(dto));

	}

	@GetMapping("/amount/{accountNo}")
	public ResponseEntity<ProcessResponseDto> amount(@PathVariable String accountNo) {
		return ResponseEntity.ok(processService.amount(accountNo));

	}

	@PostMapping("/interest-earn/{accountNo}")
	public ResponseEntity<ProcessResponseDto> earnInterest(@PathVariable String accountNo) {
		return ResponseEntity.ok(processService.earnInterest(accountNo));
	}

	@GetMapping("/account-history/{accountNo}")
	public ResponseEntity<List<ProcessResponseDto>> accountHistory(@PathVariable String accountNo) {
		return ResponseEntity.ok(processService.accountHistory(accountNo));
	}
}
