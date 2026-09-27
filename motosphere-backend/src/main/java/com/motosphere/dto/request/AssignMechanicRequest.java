package com.motosphere.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AssignMechanicRequest {
	@NotNull(message = "mechanicId is required")
	private Long mechanicId;
}
