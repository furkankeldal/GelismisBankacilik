package com.example.OnlineBankacilik.controller;

import java.util.List;

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
import com.example.OnlineBankacilik.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Hesap Yönetimi", description = "Hesap açma, sorgulama ve kapatma işlemleri")
@RequiredArgsConstructor
public class AccountController {

	private final AccountService accountService;

	@PostMapping
	@Operation(summary = "Yeni hesap aç", description = "Müşteri için yeni bir hesap açar (Vadesiz veya Vadeli)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Hesap başarıyla açıldı"),
			@ApiResponse(responseCode = "400", description = "Geçersiz istek"),
			@ApiResponse(responseCode = "404", description = "Müşteri bulunamadı")
	})
	public ResponseEntity<AccountResponseDto> openAnAcoount(@Valid @RequestBody AccountRequestDto dto) {
		return ResponseEntity.ok(accountService.accountOpen(dto));
	}

	@GetMapping
	@Operation(summary = "Tüm hesapları listele", description = "Sistemdeki tüm hesapları getirir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Hesaplar başarıyla getirildi"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
		return ResponseEntity.ok(accountService.allAccounts());
	}

	@GetMapping("/{accountNo}")
	@Operation(summary = "Hesap bilgisi getir", description = "Hesap numarasına göre hesap bilgilerini getirir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Hesap bilgisi başarıyla getirildi"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<AccountResponseDto> getByIdAccount(
			@Parameter(description = "Hesap numarası", required = true) @PathVariable String accountNo) {
		return ResponseEntity.ok(accountService.getAccount(accountNo));
	}

	@GetMapping("/customer/{customerId}")
	@Operation(summary = "Müşteri hesaplarını listele", description = "Belirli bir müşterinin tüm hesaplarını getirir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Müşteri hesapları başarıyla getirildi"),
			@ApiResponse(responseCode = "404", description = "Müşteri bulunamadı")
	})
	public ResponseEntity<List<AccountResponseDto>> getByIdCustomer(
			@Parameter(description = "Müşteri ID", required = true) @PathVariable Long customerId) {
		return ResponseEntity.ok(accountService.customerAccounts(customerId));
	}

	@DeleteMapping("/{accountNo}")
	@Operation(summary = "Hesabı kapat", description = "Belirtilen hesabı kapatır (aktif durumunu false yapar)")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Hesap başarıyla kapatıldı"),
			@ApiResponse(responseCode = "404", description = "Hesap bulunamadı")
	})
	public ResponseEntity<Void> closeAccount(
			@Parameter(description = "Hesap numarası", required = true) @PathVariable String accountNo) {
		accountService.closeAccount(accountNo);
		return ResponseEntity.noContent().build();
	}
}
