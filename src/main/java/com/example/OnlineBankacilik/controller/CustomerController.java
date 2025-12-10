package com.example.OnlineBankacilik.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.OnlineBankacilik.dto.CustomerRequestDto;
import com.example.OnlineBankacilik.dto.CustomerResponseDto;
import com.example.OnlineBankacilik.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "Müşteri Yönetimi", description = "Müşteri ekleme, güncelleme, sorgulama ve silme işlemleri")
@RequiredArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	@PostMapping
	@Operation(summary = "Yeni müşteri ekle", description = "Sisteme yeni bir müşteri ekler")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Müşteri başarıyla eklendi"),
			@ApiResponse(responseCode = "400", description = "Geçersiz istek")
	})
	public ResponseEntity<CustomerResponseDto> addCustomer(@Valid @RequestBody CustomerRequestDto dto) {
		return ResponseEntity.ok(customerService.add(dto));
	}

	@GetMapping
	@Operation(summary = "Tüm müşterileri listele", description = "Sistemdeki tüm müşterileri getirir")
	@ApiResponse(responseCode = "200", description = "Müşteriler başarıyla getirildi")
	public ResponseEntity<List<CustomerResponseDto>> getAllCustomer() {
		return ResponseEntity.ok(customerService.allList());
	}

	@GetMapping("/{customerId}")
	@Operation(summary = "Müşteri bilgisi getir", description = "Müşteri ID'sine göre müşteri bilgilerini getirir")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Müşteri bilgisi başarıyla getirildi"),
			@ApiResponse(responseCode = "404", description = "Müşteri bulunamadı")
	})
	public ResponseEntity<CustomerResponseDto> getByCustomerId(
			@Parameter(description = "Müşteri ID", required = true) @PathVariable Long customerId) {
		return ResponseEntity.ok(customerService.getById(customerId));
	}

	@PutMapping("/{customerId}")
	@Operation(summary = "Müşteri bilgilerini güncelle", description = "Mevcut müşteri bilgilerini günceller")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "Müşteri bilgileri başarıyla güncellendi"),
			@ApiResponse(responseCode = "400", description = "Geçersiz istek"),
			@ApiResponse(responseCode = "404", description = "Müşteri bulunamadı")
	})
	public ResponseEntity<CustomerResponseDto> update(
			@Parameter(description = "Müşteri ID", required = true) @PathVariable Long customerId,
			@Valid @RequestBody CustomerRequestDto dto) {
		return ResponseEntity.ok(customerService.update(customerId, dto));
	}

	@DeleteMapping("/{customerId}")
	@Operation(summary = "Müşteriyi sil", description = "Belirtilen müşteriyi sistemden siler")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "204", description = "Müşteri başarıyla silindi"),
			@ApiResponse(responseCode = "404", description = "Müşteri bulunamadı")
	})
	public ResponseEntity<Void> deleteCustomerById(
			@Parameter(description = "Müşteri ID", required = true) @PathVariable Long customerId) {
		customerService.delete(customerId);
		return ResponseEntity.noContent().build();
	}

}
