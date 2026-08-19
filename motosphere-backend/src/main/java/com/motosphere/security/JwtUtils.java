package com.motosphere.security;

import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtUtils {
	@Value("${jwt.expiration.time}")
	private long expTime;

	@Value("${jwt.secret.key}")
	private String key;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		secretKey = Keys.hmacShaKeyFor(key.getBytes());
	}

	public String generateJwt(CustomUserDetails userDetails) {
		Date iat = new Date();
		Date expiresAt = new Date(iat.getTime() + expTime);
		return Jwts.builder()
				.subject(userDetails.getUsername())
				.issuedAt(iat)
				.expiration(expiresAt)
				.claims(Map.of("user_id", userDetails.getUser().getUserId(), "user_role",
						userDetails.getUser().getRole().name()))
				.signWith(secretKey)
				.compact();
	}

	Claims validateToken(String jwt) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();
	}

}
