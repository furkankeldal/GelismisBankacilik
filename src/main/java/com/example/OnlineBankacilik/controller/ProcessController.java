package com.example.OnlineBankacilik.controller;

import java.util.List;

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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/process")
@Tag(name = "İşlem Yönetimi", description = "Para yatırma, çekme, faiz işleme ve işlem geçmişi sorgulama")
@RequiredArgsConstructor
public class ProcessController {

	private final ProcessService processService;

	@PostMapping("/deposit-money")
	@Operation(summary = "Para yatır", description = "Belirtilen hesaba para yatırır")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Para başarıyla yatırıldı"),
			@ApiResponse(responseCode = "400", description = "Geçersiz tutar"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<ProcessResponseDto> depositMoney(@Valid @RequestBody ProcessRequestDto dto) {
		return ResponseEntity.ok(processService.deposit(dto));
	}

	@PostMapping("/withdraw-money")
	@Operation(summary = "Para çek", description = "Belirtilen hesaptan para çeker")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Para başarıyla çekildi"),
			@ApiResponse(responseCode = "400", description = "Geçersiz tutar veya yetersiz bakiye"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<ProcessResponseDto> withdrawMoney(@Valid @RequestBody ProcessRequestDto dto) {
		return ResponseEntity.ok(processService.withdraw(dto));
	}

	@GetMapping("/amount/{accountNo}")
	@Operation(summary = "Hesap bakiyesini sorgula", description = "Belirtilen hesabın güncel bakiyesini getirir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Bakiye başarıyla getirildi"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<ProcessResponseDto> amount(
			@Parameter(description = "Hesap numarası", required = true) @PathVariable String accountNo) {
		return ResponseEntity.ok(processService.amount(accountNo));
	}

	@PostMapping("/interest-earn/{accountNo}")
	@Operation(summary = "Faiz işle", description = "Vadeli hesap için faiz işleme yapar")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Faiz başarıyla işlendi"),
			@ApiResponse(responseCode = "400", description = "Sadece vadeli hesaplarda faiz işlemi yapılabilir"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<ProcessResponseDto> earnInterest(
			@Parameter(description = "Hesap numarası", required = true) @PathVariable String accountNo) {
		return ResponseEntity.ok(processService.earnInterest(accountNo));
	}

	@GetMapping("/account-history/{accountNo}")
	@Operation(summary = "İşlem geçmişini getir", description = "Belirtilen hesabın tüm işlem geçmişini getirir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "İşlem geçmişi başarıyla getirildi"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<List<ProcessResponseDto>> accountHistory(
			@Parameter(description = "Hesap numarası", required = true) @PathVariable String accountNo) {
		return ResponseEntity.ok(processService.accountHistory(accountNo));
	}
}
