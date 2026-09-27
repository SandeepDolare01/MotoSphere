package com.motosphere.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// SUPER_ADMIN sets the commission terms as part of approving a pending garage
@Getter
@Setter
public class ApproveGarageRequest {
	@NotNull(message = "Commission percentage is required")
	@DecimalMin(value = "0.0", message = "Commission percentage can't be negative")
	@DecimalMax(value = "100.0", message = "Commission percentage can't exceed 100")
	private Double commissionPercentage;
}
