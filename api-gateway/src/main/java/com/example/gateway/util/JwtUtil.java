package com.example.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT Token utility class
 * Token generation ve validation işlemlerini yapar
 */
@Component
public class JwtUtil {

	@Value("${app.jwt.secret:mySecretKeyForJWTTokenGenerationAndValidationMustBeAtLeast256BitsLong}")
	private String secret;

	@Value("${app.jwt.expiration:86400000}") // 24 saat (ms)
	private Long expiration;

	/**
	 * Secret key'i alır
	 */
	private SecretKey getSigningKey() {
		byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
		return Keys.hmacShaKeyFor(keyBytes);
	}

	/**
	 * Token'dan username'i çıkarır
	 */
	public String getUsernameFromToken(String token) {
		return getClaimFromToken(token, Claims::getSubject);
	}

	/**
	 * Token'dan expiration date'i çıkarır
	 */
	public Date getExpirationDateFromToken(String token) {
		return getClaimFromToken(token, Claims::getExpiration);
	}

	/**
	 * Token'dan claim'i çıkarır
	 */
	public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = getAllClaimsFromToken(token);
		return claimsResolver.apply(claims);
	}

	/**
	 * Token'dan tüm claim'leri çıkarır
	 */
	private Claims getAllClaimsFromToken(String token) {
		try {
			return Jwts.parser()
					.verifyWith(getSigningKey())
					.build()
					.parseSignedClaims(token)
					.getPayload();
		} catch (Exception e) {
			throw new RuntimeException("Invalid JWT token", e);
		}
	}

	/**
	 * Token'ın süresi dolmuş mu kontrol eder
	 */
	private Boolean isTokenExpired(String token) {
		final Date expiration = getExpirationDateFromToken(token);
		return expiration.before(new Date());
	}

	/**
	 * Kullanıcı için token oluşturur
	 */
	public String generateToken(String username) {
		Map<String, Object> claims = new HashMap<>();
		return createToken(claims, username);
	}

	/**
	 * Kullanıcı için token oluşturur (ekstra claim'lerle)
	 */
	public String generateToken(String username, Map<String, Object> extraClaims) {
		Map<String, Object> claims = new HashMap<>(extraClaims);
		return createToken(claims, username);
	}

	/**
	 * Token oluşturur
	 */
	private String createToken(Map<String, Object> claims, String subject) {
		Date now = new Date();
		Date expiryDate = new Date(now.getTime() + expiration);

		return Jwts.builder()
				.claims(claims)
				.subject(subject)
				.issuedAt(now)
				.expiration(expiryDate)
				.signWith(getSigningKey())
				.compact();
	}

	/**
	 * Token'ı validate eder
	 */
	public Boolean validateToken(String token, String username) {
		final String tokenUsername = getUsernameFromToken(token);
		return (tokenUsername.equals(username) && !isTokenExpired(token));
	}

	/**
	 * Token'ın geçerli olup olmadığını kontrol eder
	 */
	public Boolean validateToken(String token) {
		try {
			getAllClaimsFromToken(token);
			return !isTokenExpired(token);
		} catch (Exception e) {
			return false;
		}
	}
}

