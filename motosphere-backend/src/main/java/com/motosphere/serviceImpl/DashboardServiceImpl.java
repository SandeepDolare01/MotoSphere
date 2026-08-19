package com.motosphere.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import com.motosphere.dto.response.AdminDashboardResponse;
import com.motosphere.dto.response.ManagerDashboardResponse;
import com.motosphere.entity.Garage;
import com.motosphere.entity.User;
import com.motosphere.exception.BadRequestException;
import com.motosphere.repository.UserRepository;
import com.motosphere.service.DashboardService;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Both dashboards now pull their numbers from payment-service, which owns
 * the actual Payment records in its own database. This service still
 * resolves "which garage does the current manager belong to" locally (Garage
 * ownership hasn't moved anywhere), then asks payment-service for that
 * garage's numbers specifically.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
	private final UserRepository userRepository;
	private final WebClient paymentServiceWebClient;

	@Value("${internal.api.key}")
	private String internalApiKey;

	@Override
	public AdminDashboardResponse getAdminDashboard() {
		return paymentServiceWebClient.get().uri("/internal/payments/admin-summary")
				.header("X-Internal-Api-Key", internalApiKey).retrieve().bodyToMono(AdminDashboardResponse.class)
				.onErrorMap(this::wrapDownstreamError).block();
	}

	@Override
	public ManagerDashboardResponse getManagerDashboard() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new BadRequestException("Invalid user!"));
		if (manager.getGarage() == null)
			throw new BadRequestException("You're not assigned to a garage yet");

		Garage garage = manager.getGarage();

		return paymentServiceWebClient.get().uri("/internal/payments/garage-summary/{garageId}", garage.getGarageId())
				.header("X-Internal-Api-Key", internalApiKey).retrieve().bodyToMono(ManagerDashboardResponse.class)
				.onErrorMap(this::wrapDownstreamError).block();
	}

	private Throwable wrapDownstreamError(Throwable e) {
		log.error("payment-service call failed: {}", e.getMessage());
		return new BadRequestException("Could not load payment data right now - please try again shortly");
	}
}
