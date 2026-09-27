package com.motosphere.payment.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDashboardResponse {
	private String garageName;
	private BigDecimal todaysEarnings;
	private BigDecimal totalEarnings;
	private long totalTransactionCount;
	private List<TransactionResponse> transactions;
}
