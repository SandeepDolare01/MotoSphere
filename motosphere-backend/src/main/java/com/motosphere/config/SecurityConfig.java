package com.motosphere.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.motosphere.security.JwtFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtFilter jwtFilter;

	// the React app runs on its own dev server (Vite's default is 5173), a
	// different origin than the API - comma-separated, override in
	// application.properties for your deployed frontend's real origin(s)
	@Value("${motosphere.cors.allowed-origins}")
	private String allowedOrigins;

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.cors(cors -> cors.configurationSource(corsConfigurationSource()));
		http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		http.authorizeHttpRequests(request -> request
				.requestMatchers("/", "/index.html", "/static/**", "/assets/**", "/*.js", "/*.css", "/favicon.ico",
						"/swagger-ui/**", "/v3/api-docs/**", "/auth/register", "/auth/register-super-admin",
						"/auth/register-garage-manager", "/auth/login")
				.permitAll()

				// Garage manager's own mechanic management - MUST precede the general
				// GET /garages/** permitAll matcher below, or it gets shadowed
				.requestMatchers(HttpMethod.POST, "/garages/my/mechanics").hasRole("GARAGE_MANAGER")
				.requestMatchers(HttpMethod.GET, "/garages/my/mechanics").hasRole("GARAGE_MANAGER")
				.requestMatchers(HttpMethod.PATCH, "/garages/my/mechanics/*/deactivate").hasRole("GARAGE_MANAGER")
				.requestMatchers(HttpMethod.PATCH, "/garages/my/mechanics/*/reactivate").hasRole("GARAGE_MANAGER")

				// Garage manager's own gallery photos - MUST precede the general
				// DELETE /garages/** (SUPER_ADMIN-only) matcher below, or it gets shadowed
				.requestMatchers(HttpMethod.POST, "/garages/my/image").hasRole("GARAGE_MANAGER")
				.requestMatchers(HttpMethod.DELETE, "/garages/my/image").hasRole("GARAGE_MANAGER")

				// Garage approval workflow - also must precede the GET /garages/** wildcard
				.requestMatchers(HttpMethod.GET, "/garages/pending").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/garages/*/approve").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/garages/*/reject").hasRole("SUPER_ADMIN")

				// Garages - public browsing (APPROVED only, filtered in the service layer), admin-only writes.
				// This also covers GET /garages/{id}/images (list) and
				// GET /garages/{id}/images/{imageId} (raw bytes) - garage photos are public.
				.requestMatchers(HttpMethod.GET, "/garages/**").permitAll()
				.requestMatchers(HttpMethod.POST, "/garages").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/garages/**").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.PUT, "/garages/**").hasAnyRole("SUPER_ADMIN", "GARAGE_MANAGER")

				// Users
				.requestMatchers(HttpMethod.POST, "/users/staff").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.GET, "/users").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/users/*/deactivate").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.PATCH, "/users/*/reactivate").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("SUPER_ADMIN")

				// Vehicles - customer only
				.requestMatchers(HttpMethod.POST, "/vehicles").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.GET, "/vehicles/my").hasRole("CUSTOMER")

				// Appointments
				.requestMatchers(HttpMethod.POST, "/appointments").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.GET, "/appointments/my").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.PATCH, "/appointments/{appointmentId}/cancel").hasRole("CUSTOMER")
				.requestMatchers(HttpMethod.GET, "/appointments/garage").hasRole("GARAGE_MANAGER")
				.requestMatchers(HttpMethod.PATCH, "/appointments/{appointmentId}/assign-mechanic")
				.hasRole("GARAGE_MANAGER")
				.requestMatchers(HttpMethod.GET, "/appointments/mechanic").hasRole("MECHANIC")

				// Job cards - mechanic writes, admin/manager/owning-customer reads (checked in service)
				.requestMatchers(HttpMethod.POST, "/jobcards/appointment/**").hasRole("MECHANIC")
				.requestMatchers(HttpMethod.POST, "/jobcards/*/items").hasRole("MECHANIC")
				.requestMatchers(HttpMethod.PATCH, "/jobcards/*/complete").hasRole("MECHANIC")

				// Payments now live entirely in payment-service (a separate app/port) -
				// this backend no longer serves any /payments/** route itself.

				// Dashboards
				.requestMatchers(HttpMethod.GET, "/dashboard/admin").hasRole("SUPER_ADMIN")
				.requestMatchers(HttpMethod.GET, "/dashboard/manager").hasRole("GARAGE_MANAGER")

				// Internal, service-to-service only (called by payment-service) - no
				// user JWT is sent on these calls, so they can't require a role here.
				// Real protection is InternalApiKeyGuard checking X-Internal-Api-Key
				// inside InternalController itself.
				.requestMatchers("/internal/**").permitAll()

				.anyRequest().authenticated());

		http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of(allowedOrigins.split(",")));
		config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
		// the frontend never reads cookies from this API (auth is a Bearer JWT held
		// in memory/localStorage), so credentials-mode CORS isn't needed here
		config.setAllowCredentials(false);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}
