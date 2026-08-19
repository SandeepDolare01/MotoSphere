package com.motosphere.payment.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motosphere.payment.exception.UnauthorizedActionException;

// Mirrors motosphere-backend's InternalApiKeyGuard - protects this
// service's own /internal/** endpoints (called by the core service to fetch
// dashboard summaries), using the same shared secret.
@Component
public class InternalApiKeyGuard {

	@Value("${internal.api.key}")
	private String expectedKey;

	public void verify(String providedKey) {
		if (providedKey == null || !providedKey.equals(expectedKey))
			throw new UnauthorizedActionException("Invalid or missing internal API key");
	}
}
