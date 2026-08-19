package com.motosphere.payment.util;

import org.springframework.security.core.context.SecurityContextHolder;

// Identical contract to motosphere-backend's SecurityUtils - reads the
// principal/authorities JwtFilter populated from the JWT claims.
public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static boolean isAuthenticated() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal instanceof Long;
	}

	public static Long getCurrentUserId() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return (Long) principal;
	}

	public static boolean currentUserHasRole(String role) {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
	}
}
