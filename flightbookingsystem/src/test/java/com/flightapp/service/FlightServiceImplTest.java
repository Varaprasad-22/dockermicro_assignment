package com.flightapp.service;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.flightapp.dto.Flight;
import com.flightapp.dto.Search;
import com.flightapp.dto.SearchResult;
import com.flightapp.exceptions.ResourceNotFoundException;
import com.flightapp.model.Airline;
import com.flightapp.model.FlightEntity;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.repository.FlightRepository;
import com.flightapp.service.FlightServiceImpl;

@ExtendWith(MockitoExtension.class)
public class FlightServiceImplTest {
	@Mock
	private FlightRepository flightRepo;

	@Mock
	private AirlineRepository airlineRepo;

	@InjectMocks
	private FlightServiceImpl flightService;
	private Airline airline;
	private Flight flightDto;
	private FlightEntity entity;

	@BeforeEach
	void setup() {
		airline = new Airline();
		airline.setAirlineId(1);
		airline.setAirlineName("Air India");

		flightDto = new Flight();
		flightDto.setAirlineName("Air India");
		flightDto.setFlightNumber("ABC-123");
		flightDto.setFromPlace("HYD");
		flightDto.setToPlace("DEL");
		flightDto.setTotalSeats(150);
		flightDto.setPrice(5000);
		flightDto.setArrivalTime(LocalDateTime.now().plusHours(2));
		flightDto.setDepatureTime(LocalDateTime.now());

		entity = new FlightEntity();
		entity.setAirlineId(1);
		entity.setFlightId(10);
		entity.setFlightNumber("ABC-123");
		entity.setFromLocation("HYD");
		entity.setToLocation("DEL");
		entity.setDepatureTime(LocalDateTime.now());
		entity.setArrivalTime(LocalDateTime.now().plusHours(2));
		entity.setPrice(5000);
		entity.setTotalSeats(150);
		entity.setAvaliSeats(150);
	}

	@Test
	void testAddFlight_success() {
		when(airlineRepo.findByAirlineName("Air India")).thenReturn(Optional.of(airline));
		when(flightRepo.findByFlightNumber("ABC-123")).thenReturn(Optional.empty());
		when(flightRepo.save(any())).thenReturn(entity);

		int flightId = flightService.addFlight(flightDto);

		assertEquals(10, flightId);
		verify(flightRepo, times(1)).save(any());
	}

	@Test
	void addFlight_airlineNotFound() {
		when(airlineRepo.findByAirlineName("Air India")).thenReturn(Optional.empty());
		assertThrows(RuntimeException.class, () -> flightService.addFlight(flightDto));
	}

	@Test
	void addFlight_duplicateFlight() {
		when(airlineRepo.findByAirlineName("Air India")).thenReturn(Optional.of(airline));
		when(flightRepo.findByFlightNumber("ABC-123")).thenReturn(Optional.of(entity));

		assertThrows(RuntimeException.class, () -> flightService.addFlight(flightDto));
	}

	@Test
	void testSearchFlights_success() {

		Search searchDto = new Search();
		searchDto.setFromPlace("HYD");
		searchDto.setToPlace("DEL");
		searchDto.setTripType("one-way");
		searchDto.setDepartureDate(LocalDate.now());

		when(flightRepo.findByFromLocationAndToLocationAndDepatureTimeBetween(anyString(), anyString(), any(), any()))
				.thenReturn(List.of(entity));

		when(airlineRepo.findByAirlineId(1)).thenReturn(Optional.of(airline));

		SearchResult result = flightService.search(searchDto);

		assertNotNull(result);
		assertEquals(1, result.getOutboundFlights().size());
	}

	@Test
	void testGetFlightById_success() {
		when(flightRepo.findById(10)).thenReturn(Optional.of(entity));
		when(airlineRepo.findByAirlineId(1)).thenReturn(Optional.of(airline));

		var res = flightService.getById(10);

		assertEquals("Air India", res.getBody().getAirlineName());
		assertEquals("HYD", res.getBody().getFromPlace());
	}

	@Test
	void getFlightDetails_notFound() {
		when(flightRepo.findById(10)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> flightService.getById(10));
	}

	@Test
	void updateSeats_flightNotFound() {
		when(flightRepo.findById(10)).thenReturn(Optional.empty());
		assertThrows(ResourceNotFoundException.class, () -> flightService.updateDetails(10, -1));
	}

	@Test
	void testUpdateSeats_success() {
		when(flightRepo.findById(10)).thenReturn(Optional.of(entity));

		flightService.updateDetails(10, -2);

		verify(flightRepo, times(1)).save(any());
		assertEquals(148, entity.getAvaliSeats());
	}
}
