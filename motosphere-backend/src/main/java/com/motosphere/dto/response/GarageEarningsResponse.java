package com.motosphere.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GarageEarningsResponse {
	private Long garageId;
	private String garageName;
	private long transactionCount;
	private BigDecimal totalRevenue; // sum of amountPaid (GST included)
	private BigDecimal totalCommission; // MotoSphere's cut across this garage's payments
	private BigDecimal totalGarageAmount; // what this garage actually received
}
