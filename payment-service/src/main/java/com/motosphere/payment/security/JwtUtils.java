package com.motosphere.payment.security;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

// Validate-only - this service never issues tokens (that's still
// motosphere-backend's /auth/login), it just needs to verify a token it's
// handed really was signed by the core service. Requires the exact same
// jwt.secret.key value as motosphere-backend for the signature to check out.
@Component
public class JwtUtils {
	@Value("${jwt.secret.key}")
	private String key;

	private SecretKey secretKey;

	@PostConstruct
	public void init() {
		secretKey = Keys.hmacShaKeyFor(key.getBytes());
	}

	Claims validateToken(String jwt) {
		return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(jwt).getPayload();
	}
}
