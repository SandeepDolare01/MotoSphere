package com.motosphere.dto.request;

import java.math.BigDecimal;

import com.motosphere.enums.ItemType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * The mechanic supplies description/quantity/unitPrice only - amount is
 * ALWAYS computed server-side (quantity * unitPrice), never accepted from
 * the client.
 */
@Getter
@Setter
public class JobCardItemRequest {
	@NotBlank(message = "description is required")
	private String description;

	@NotNull(message = "itemType is required")
	private ItemType itemType;

	@NotNull(message = "quantity is required")
	@Positive(message = "quantity must be positive")
	private Integer quantity;

	@NotNull(message = "unitPrice is required")
	@Positive(message = "unitPrice must be positive")
	private BigDecimal unitPrice;
}
