package com.motosphere.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobCardRequest {
	private String diagnosis;
	private String remarks;

	@PositiveOrZero(message = "labourCharge can't be negative")
	private BigDecimal labourCharge;
}
