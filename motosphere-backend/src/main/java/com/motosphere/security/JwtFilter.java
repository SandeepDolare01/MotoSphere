package com.motosphere.security;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Runs once per request. Reads the Bearer JWT (if present), validates it, and
 * populates the SecurityContext with the user's id (as principal) and role
 * (as a single GrantedAuthority) so downstream hasRole(...) checks and
 * SecurityUtils.getCurrentUserId() both work without hitting the DB again.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {
	private final JwtUtils jwtUtils;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			String headerValue = request.getHeader("Authorization");
			if (headerValue != null && headerValue.startsWith("Bearer ")) {
				String jwt = headerValue.substring(7);
				Claims claims = jwtUtils.validateToken(jwt);

				Long userId = claims.get("user_id", Long.class);
				String role = claims.get("user_role", String.class);

				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null,
						List.of(new SimpleGrantedAuthority("ROLE_" + role)));
				SecurityContextHolder.getContext().setAuthentication(auth);
			} else {
				log.debug("No JWT present - request proceeds unauthenticated");
			}
			filterChain.doFilter(request, response);

		} catch (Exception e) {
			SecurityContextHolder.clearContext();
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			response.getWriter().print("Invalid or expired token!");
		}
	}

}
