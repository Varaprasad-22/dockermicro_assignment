package com.bookingapp.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bookingapp.dto.BookingGetResponse;
import com.bookingapp.dto.Bookingdto;
import com.bookingapp.service.BookingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flight")
public class FlightBookingController {
	private final BookingService bookingService;

	public FlightBookingController(BookingService bookingService) {
		this.bookingService = bookingService;
	}

	@PostMapping("/booking")
	public ResponseEntity<String> flightBooking(@Valid @RequestBody Bookingdto data) {
		String result = bookingService.bookFlight(data);
		return ResponseEntity.status(HttpStatus.CREATED).body(result);
	}

	@GetMapping("/ticket/{pnr}")
	public ResponseEntity<BookingGetResponse> bookingDetails(@PathVariable String pnr) {
		BookingGetResponse a = null;
		a = bookingService.getBookingDetails(pnr);
		if (a == null) {
			return ResponseEntity.notFound().build();
		} else {
			return ResponseEntity.ok(a);
		}
	}

	@GetMapping("/booking/history/{emailId}")
	public List<Bookingdto> getHistoryByEmail(@PathVariable String emailId) {
		return bookingService.getHistoryByEmail(emailId);
	}

	@DeleteMapping("/booking/cancel/{pnr}")
	public String cancelBooking(@Valid @PathVariable String pnr) {
		return bookingService.cancelTicket(pnr);
	}
}
