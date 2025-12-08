package com.flightapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flightapp.controller.FlightController;
import com.flightapp.dto.Flight;
import com.flightapp.dto.Search;
import com.flightapp.dto.SearchResult;
import com.flightapp.service.FlightService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(FlightController.class)
public class FlightControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private FlightService flightService;

	@Autowired
	private ObjectMapper objectMapper;

	private Flight flightDto;
	private Search searchDto;

	@BeforeEach
	void setup() {
		flightDto = new Flight();
		flightDto.setAirlineName("Air India");
		flightDto.setFlightNumber("AI-101");
		flightDto.setFromPlace("HYD");
		flightDto.setToPlace("DEL");
		flightDto.setDepatureTime(LocalDateTime.now());
		flightDto.setArrivalTime(LocalDateTime.now().plusHours(2));
		flightDto.setTotalSeats(150);
		flightDto.setPrice(5000);

		searchDto = new Search();
		searchDto.setFromPlace("HYD");
		searchDto.setToPlace("DEL");
		searchDto.setTripType("one-way");
		searchDto.setDepartureDate(LocalDate.now());
	}

	@Test
	void testAddFlight() throws Exception {
		when(flightService.addFlight(any())).thenReturn(10);

		mockMvc.perform(post("/api/flights/airline/inventory").contentType("application/json")
				.content(objectMapper.writeValueAsString(flightDto))).andExpect(status().isCreated())
				.andExpect(content().string("10"));
	}

	@Test
	void testSearchFlights() throws Exception {
		SearchResult result = new SearchResult();
		result.setOutboundFlights(List.of(flightDto));

		when(flightService.search(any())).thenReturn(result);

		mockMvc.perform(post("/api/flights/search").contentType("application/json")
				.content(objectMapper.writeValueAsString(searchDto))).andExpect(status().isOk())
				.andExpect(jsonPath("$.outboundFlights").isArray());
	}

	@Test
	void testGetFlightDetails() throws Exception {
		when(flightService.getById(10)).thenReturn(ResponseEntity.ok(flightDto));

		mockMvc.perform(get("/api/flights/10")).andExpect(status().isOk())
				.andExpect(jsonPath("$.flightNumber").value("AI-101"));
	}
}
