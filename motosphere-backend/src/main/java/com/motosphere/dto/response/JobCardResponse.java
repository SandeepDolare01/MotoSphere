package com.motosphere.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class JobCardResponse {
	private Long jobCardId;
	private String diagnosis;
	private String remarks;
	private BigDecimal labourCharge;
	private LocalDate completionDate;
	private Long appointmentId;
	private String mechanicName;
	private List<JobCardItemResponse> items;
}
