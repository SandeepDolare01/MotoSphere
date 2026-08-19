package com.motosphere.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManagerDashboardResponse {
	private String garageName;
	private BigDecimal todaysEarnings; // this garage's share (post-commission) of today's payments
	private BigDecimal totalEarnings; // this garage's share, all-time
	private long totalTransactionCount;
	private List<TransactionResponse> transactions;
}
