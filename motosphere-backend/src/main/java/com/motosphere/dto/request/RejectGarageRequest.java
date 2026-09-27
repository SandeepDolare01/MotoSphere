package com.motosphere.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RejectGarageRequest {
	private String reason; // optional, shown to nobody automatically yet - stored for audit/reference
}
