package com.bookingapp;

import com.bookingapp.controller.FlightBookingController;
import com.bookingapp.dto.*;
import com.bookingapp.service.BookingService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlightBookingController.class)
public class FlightBooingControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private BookingService bookingService;

	@Autowired
	private ObjectMapper objectMapper;

	private Bookingdto bookingDto;
	private BookingGetResponse bookingResponse;
	private Passengers passenger;

	@BeforeEach
	void setup() {

		passenger = new Passengers();
		passenger.setName("Vara");
		passenger.setAge(25);
		passenger.setGender("Male");
		passenger.setMeal("Veg");
		passenger.setSeatNo("12A");

		bookingDto = new Bookingdto();
		bookingDto.setEmailId("virupavaraprasad22@gmail.com");
		bookingDto.setName("Vara");
		bookingDto.setNoOfSeats(1);
		bookingDto.setOutboundFlightId(10);
		bookingDto.setPassengers(List.of(passenger));

		bookingResponse = new BookingGetResponse();
		bookingResponse.setPnr("ABC123");
		bookingResponse.setFlightId("10");
		bookingResponse.setPassengersList(List.of(passenger));
	}

	@Test
	void testFlightBooking_success() throws Exception {

		when(bookingService.bookFlight(any())).thenReturn("One-way Booking Successful! PNR: ABC123");

		mockMvc.perform(post("/api/flight/booking").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(bookingDto))).andExpect(status().isCreated())
				.andExpect(content().string("One-way Booking Successful! PNR: ABC123"));
	}

	@Test
	void testBookingDetails_success() throws Exception {

		when(bookingService.getBookingDetails("ABC123")).thenReturn(bookingResponse);

		mockMvc.perform(get("/api/flight/ticket/ABC123")).andExpect(status().isOk())
				.andExpect(jsonPath("$.pnr").value("ABC123"));
	}

	@Test
	void testGetHistoryByEmail_success() throws Exception {

		when(bookingService.getHistoryByEmail("virupavaraprasad22@gmail.com")).thenReturn(List.of(bookingDto));

		mockMvc.perform(get("/api/flight/booking/history/virupavaraprasad22@gmail.com")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].emailId").value("virupavaraprasad22@gmail.com"));
	}

	@Test
	void testCancelBooking_success() throws Exception {

		when(bookingService.cancelTicket("ABC123")).thenReturn("Ticket with PNR ABC123 successfully cancelled.");

		mockMvc.perform(delete("/api/flight/booking/cancel/ABC123")).andExpect(status().isOk())
				.andExpect(content().string("Ticket with PNR ABC123 successfully cancelled."));
	}
}
