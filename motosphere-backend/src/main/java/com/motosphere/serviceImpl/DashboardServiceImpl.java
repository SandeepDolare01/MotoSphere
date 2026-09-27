package com.motosphere.serviceImpl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.client.PaymentServiceFeignClient;
import com.motosphere.dto.response.AdminDashboardResponse;
import com.motosphere.dto.response.ManagerDashboardResponse;
import com.motosphere.entity.Garage;
import com.motosphere.entity.User;
import com.motosphere.exception.BadRequestException;
import com.motosphere.repository.UserRepository;
import com.motosphere.service.DashboardService;
import com.motosphere.util.SecurityUtils;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Both dashboards now pull their numbers from payment-service, which owns
 * the actual Payment records in its own database. This service still
 * resolves "which garage does the current manager belong to" locally (Garage
 * ownership hasn't moved anywhere), then asks payment-service for that
 * garage's numbers specifically.
 *
 * Migrated from WebClient to a declarative Feign client
 * (PaymentServiceFeignClient) - the calls are now plain blocking method
 * calls instead of a reactive Mono pipeline, so error handling is a regular
 * try/catch instead of .onErrorMap(...).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
	private final UserRepository userRepository;
	private final PaymentServiceFeignClient paymentServiceFeignClient;

	@Override
	public AdminDashboardResponse getAdminDashboard() {
		try {
			return paymentServiceFeignClient.getAdminSummary();
		} catch (FeignException e) {
			throw wrapDownstreamError(e);
		}
	}

	@Override
	public ManagerDashboardResponse getManagerDashboard() {
		Long currentUserId = SecurityUtils.getCurrentUserId();
		User manager = userRepository.findById(currentUserId)
				.orElseThrow(() -> new BadRequestException("Invalid user!"));
		if (manager.getGarage() == null)
			throw new BadRequestException("You're not assigned to a garage yet");

		Garage garage = manager.getGarage();

		try {
			return paymentServiceFeignClient.getGarageSummary(garage.getGarageId());
		} catch (FeignException e) {
			throw wrapDownstreamError(e);
		}
	}

	private BadRequestException wrapDownstreamError(FeignException e) {
		log.error("payment-service call failed: {}", e.getMessage());
		return new BadRequestException("Could not load payment data right now - please try again shortly");
	}
}
