package com.motosphere.serviceImpl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.motosphere.dto.request.JobCardRequest;
import com.motosphere.dto.response.ApiResponse;
import com.motosphere.dto.response.JobCardItemResponse;
import com.motosphere.dto.response.JobCardResponse;
import com.motosphere.entity.Appointment;
import com.motosphere.entity.Invoice;
import com.motosphere.entity.JobCard;
import com.motosphere.enums.AppointmentStatus;
import com.motosphere.enums.Role;
import com.motosphere.exception.BadRequestException;
import com.motosphere.exception.ResourceNotFoundException;
import com.motosphere.exception.UnauthorizedActionException;
import com.motosphere.repository.AppointmentRepository;
import com.motosphere.repository.InvoiceRepository;
import com.motosphere.repository.JobCardRepository;
import com.motosphere.repository.UserRepository;
import com.motosphere.service.JobCardService;
import com.motosphere.util.InvoiceNumberGenerator;
import com.motosphere.util.SecurityUtils;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class JobCardServiceImpl implements JobCardService {
	private final JobCardRepository jobCardRepository;
	private final AppointmentRepository appointmentRepository;
	private final InvoiceRepository invoiceRepository;
	private final UserRepository userRepository;
	private final InvoiceNumberGenerator invoiceNumberGenerator;

	@Value("${invoice.gst.percentage}")
	private double gstPercentage;

	@Override
	public ApiResponse createJobCard(Long appointmentId, JobCardRequest request) {
		Appointment appointment = appointmentRepository.findById(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid appointmentId!"));

		Long currentUserId = SecurityUtils.getCurrentUserId();
		if (appointment.getMechanic() == null || !appointment.getMechanic().getUserId().equals(currentUserId))
			throw new UnauthorizedActionException("This appointment is not assigned to you");

		if (appointment.getStatus() != AppointmentStatus.ASSIGNED)
			throw new BadRequestException("A job card can only be created for an assigned appointment");

		if (jobCardRepository.existsByAppointment_AppointmentId(appointmentId))
			throw new BadRequestException("This appointment already has a job card");

		JobCard jobCard = new JobCard(request.getDiagnosis(), request.getRemarks(), request.getLabourCharge());
		jobCard.setAppointment(appointment);
		jobCard.setMechanic(appointment.getMechanic());
		jobCardRepository.save(jobCard);

		appointment.setStatus(AppointmentStatus.IN_PROGRESS);

		return new ApiResponse("Job card created!", "Success");
	}

	@Override
	public JobCardResponse getJobCard(Long jobCardId) {
		JobCard jobCard = jobCardRepository.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid jobCardId!"));

		checkViewAccess(jobCard);

		return toDto(jobCard);
	}

	@Override
	public JobCardResponse getJobCardByAppointmentId(Long appointmentId) {
		JobCard jobCard = jobCardRepository.findByAppointment_AppointmentId(appointmentId)
				.orElseThrow(() -> new ResourceNotFoundException("No job card exists for this appointment yet"));

		checkViewAccess(jobCard);

		return toDto(jobCard);
	}

	@Override
	public ApiResponse completeJobCard(Long jobCardId) {
		JobCard jobCard = jobCardRepository.findById(jobCardId)
				.orElseThrow(() -> new ResourceNotFoundException("Invalid jobCardId!"));

		Long currentUserId = SecurityUtils.getCurrentUserId();
		if (!jobCard.getMechanic().getUserId().equals(currentUserId))
			throw new UnauthorizedActionException("This job card is not assigned to you");

		if (jobCard.getCompletionDate() != null)
			throw new BadRequestException("This job card is already completed");

		jobCard.setCompletionDate(LocalDate.now());
		jobCard.getAppointment().setStatus(AppointmentStatus.COMPLETED);

		generateInvoice(jobCard);

		return new ApiResponse("Job completed, invoice generated!", "Success");
	}

	// --- helpers ---

	private void generateInvoice(JobCard jobCard) {
		if (invoiceRepository.existsByJobCard_JobCardId(jobCard.getJobCardId()))
			return; // idempotency guard

		BigDecimal itemsTotal = jobCard.getItems().stream().map(i -> i.getAmount()).reduce(BigDecimal.ZERO,
				BigDecimal::add);
		BigDecimal subtotal = itemsTotal.add(jobCard.getLabourCharge()).setScale(2, RoundingMode.HALF_UP);
		BigDecimal gstAmount = subtotal.multiply(BigDecimal.valueOf(gstPercentage))
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
		BigDecimal totalAmount = subtotal.add(gstAmount).setScale(2, RoundingMode.HALF_UP);

		Invoice invoice = new Invoice(invoiceNumberGenerator.generate(jobCard.getJobCardId()), LocalDate.now(),
				subtotal, gstAmount, totalAmount);
		invoice.setGstPercentage(gstPercentage);
		invoice.setJobCard(jobCard);
		invoiceRepository.save(invoice);
	}

	private void checkViewAccess(JobCard jobCard) {
		if (SecurityUtils.currentUserHasRole(Role.SUPER_ADMIN))
			return;

		Long currentUserId = SecurityUtils.getCurrentUserId();
		Appointment appointment = jobCard.getAppointment();

		boolean isOwningCustomer = appointment.getVehicle().getCustomer().getUserId().equals(currentUserId);
		boolean isAssignedMechanic = jobCard.getMechanic().getUserId().equals(currentUserId);

		boolean managesThisGarage = false;
		if (SecurityUtils.currentUserHasRole(Role.GARAGE_MANAGER)) {
			var manager = userRepository.findById(currentUserId).orElse(null);
			managesThisGarage = manager != null && manager.getGarage() != null
					&& manager.getGarage().getGarageId().equals(appointment.getGarage().getGarageId());
		}

		if (!isOwningCustomer && !isAssignedMechanic && !managesThisGarage)
			throw new UnauthorizedActionException("You don't have access to this job card");
	}

	private JobCardResponse toDto(JobCard jobCard) {
		List<JobCardItemResponse> items = jobCard.getItems().stream()
				.map(i -> new JobCardItemResponse(i.getJobCardItemId(), i.getDescription(), i.getItemType(),
						i.getQuantity(), i.getUnitPrice(), i.getAmount()))
				.toList();
		String mechanicName = jobCard.getMechanic().getFirstName() + " " + jobCard.getMechanic().getLastName();
		return new JobCardResponse(jobCard.getJobCardId(), jobCard.getDiagnosis(), jobCard.getRemarks(),
				jobCard.getLabourCharge(), jobCard.getCompletionDate(), jobCard.getAppointment().getAppointmentId(),
				mechanicName, items);
	}

}
