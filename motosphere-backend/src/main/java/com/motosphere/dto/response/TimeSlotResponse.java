package com.motosphere.dto.response;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

// A single bookable 30-min window. Only slots that currently have at least
// one free mechanic are ever returned by AppointmentService.getAvailableSlots()
// - there's no "unavailable" flag here on purpose, because the requirement is
// for fully-booked slots to disappear entirely, not show up disabled.
@Getter
@Setter
@AllArgsConstructor
public class TimeSlotResponse {
	private LocalTime startTime;
	private LocalTime endTime;
}
