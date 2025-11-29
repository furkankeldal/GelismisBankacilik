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

	@ExceptionHandler({ AccountNotFoundException.class, CustomerNotFoundException.class,
			InsufficientBalanceException.class, InvalidAmountException.class })
	public ResponseEntity<Map<String, Object>> handleGlobal(RuntimeException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("Hata", ex.getClass().getSimpleName());
		body.put("mesaj", ex.getMessage());
		body.put("zaman", LocalDateTime.now().toString());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, Object> body = new LinkedHashMap<>();
		body.put("Hata", "Validation error");
		body.put("zaman", LocalDateTime.now().toString());
		List<String> errors = ex.getBindingResult().getFieldErrors().stream()
				.map(e -> e.getField() + ": " + e.getDefaultMessage()).toList();
		body.put("errors", errors);
		return ResponseEntity.badRequest().body(body);
	}

}
