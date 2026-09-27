package com.motosphere.util;

import org.springframework.security.core.context.SecurityContextHolder;

import com.motosphere.enums.Role;

/**
 * Pulls the authenticated user's id/role straight from the SecurityContext
 * (populated by JwtFilter from the JWT claims), rather than trusting a
 * path/body-supplied id. Every "my own resource" check in this project
 * (my vehicles, my appointments, my job cards, my garage) MUST route through
 * this rather than accept an id from the client.
 */
public final class SecurityUtils {

	private SecurityUtils() {
	}

	/**
	 * True only for a genuine JWT-authenticated caller (principal is the Long
	 * user id set by JwtFilter). False for anonymous/unauthenticated requests,
	 * whose principal is the string "anonymousUser" - callers on a permitAll
	 * endpoint MUST check this before calling getCurrentUserId(), or an
	 * anonymous request will throw a ClassCastException instead of a clean
	 * auth failure.
	 */
	public static boolean isAuthenticated() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return principal instanceof Long;
	}

	public static Long getCurrentUserId() {
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		return (Long) principal;
	}

	public static boolean currentUserHasRole(Role role) {
		return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_" + role.name()));
	}

}
