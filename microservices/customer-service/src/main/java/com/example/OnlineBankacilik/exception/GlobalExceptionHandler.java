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

	@ExceptionHandler(CustomerNotFoundException.class)
	public ResponseEntity<Map<String, Object>> handleCustomerNotFound(CustomerNotFoundException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
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

