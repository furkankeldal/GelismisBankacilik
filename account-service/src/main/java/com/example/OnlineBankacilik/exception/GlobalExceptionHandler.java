package com.example.OnlineBankacilik.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(AccountNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleAccountNotFound(AccountNotFoundException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
	}

	@ExceptionHandler(InsufficientBalanceException.class)
	public ResponseEntity<Map<String, Object>> handleInsufficientBalance(InsufficientBalanceException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
	}

	@ExceptionHandler(InvalidAmountException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidAmount(InvalidAmountException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(InvalidAccountTypeException.class)
	public ResponseEntity<Map<String, Object>> handleInvalidAccountType(InvalidAccountTypeException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", "ValidationError");
		body.put("mesaj", "Girilen veriler geçersiz");
		body.put("zaman", LocalDateTime.now());
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
		body.put("errors", errors);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

}
