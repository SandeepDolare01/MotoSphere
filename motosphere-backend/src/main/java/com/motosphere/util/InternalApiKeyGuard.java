package com.motosphere.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.motosphere.exception.UnauthorizedActionException;

/**
 * Internal endpoints (/internal/**) are called service-to-service by
 * payment-service, never by a browser - they carry no user JWT, so they're
 * marked permitAll in SecurityConfig and instead protected by this shared
 * secret header. Both services must be configured with the same
 * internal.api.key value.
 *
 * This is a deliberately simple mechanism appropriate for a small,
 * same-network deployment. A larger system would use mTLS or a service mesh
 * instead, but a shared static key is the standard "good enough" starting
 * point for service-to-service auth between two trusted internal services.
 */
@Component
public class InternalApiKeyGuard {

	@Value("${internal.api.key}")
	private String expectedKey;

	public void verify(String providedKey) {
		if (providedKey == null || !providedKey.equals(expectedKey))
			throw new UnauthorizedActionException("Invalid or missing internal API key");
	}
}
