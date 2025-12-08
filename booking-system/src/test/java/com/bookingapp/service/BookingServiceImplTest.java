package com.bookingapp.service;

import com.bookingapp.client.FlightServiceClient;
import com.bookingapp.dto.*;
import com.bookingapp.exceptions.BookingException;
import com.bookingapp.exceptions.ResourceNotFoundException;
import com.bookingapp.model.BookingEntity;
import com.bookingapp.model.PassengerEntity;
import com.bookingapp.model.User;
import com.bookingapp.producer.BookingProducer;
import com.bookingapp.repository.BookingRepository;
import com.bookingapp.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookingServiceImplTest {
	@InjectMocks
	private BookingServiceImpl bookingService;

	@Mock
	private BookingRepository bookingRepo;

	@Mock
	private UserRepository userRepo;

	@Mock
	private FlightServiceClient flightClient;

	@Mock
	private BookingProducer producer;

	private Bookingdto bookingDto;
	private User user;
	private FlightDto flightDto;
	private BookingEntity bookingEntity;

	@BeforeEach
	void setup() {

		user = new User();
		user.setUserId("USER1");
		user.setEmail("virupavaraprasad@gmail.com");
		user.setName("vara");

		Passengers passenger = new Passengers();
		passenger.setName("vara");
		passenger.setAge(25);
		passenger.setMeal("Veg");
		passenger.setGender("M");
		passenger.setSeatNo("12A");

		bookingDto = new Bookingdto();
		bookingDto.setEmailId("virupavaraprasad@gmail.com");
		bookingDto.setName("vara");
		bookingDto.setOutboundFlightId(10);
		bookingDto.setNoOfSeats(1);
		bookingDto.setPassengers(List.of(passenger));

		flightDto = new FlightDto();
		flightDto.setFlightId(10);
		flightDto.setDepatureTime(LocalDateTime.now().plusHours(25));
		flightDto.setTotalSeats(10);

		bookingEntity = new BookingEntity();
		bookingEntity.setPnr("PNR1234");
		bookingEntity.setEmailId("virupavaraprasad@gmail.com");
		bookingEntity.setFlightId(10);
	}

	@Test
	void testBookFlight_success_oneWay() {

		when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.of(flightDto));
		when(bookingRepo.save(any())).thenReturn(bookingEntity);

		String result = bookingService.bookFlight(bookingDto);

		assertTrue(result.contains("One-way Booking Successful"));
		verify(flightClient, times(1)).updateSeats(10, -1);
	}

	@Test
	void testBookFlight_success_roundTrip() {
		Integer returnFlightId = 20;
		bookingDto.setReturnFlightId(returnFlightId);

		FlightDto returnFlightDto = new FlightDto();
		returnFlightDto.setFlightId(returnFlightId);
		returnFlightDto.setTotalSeats(10);

		when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.of(flightDto));
		when(flightClient.getFlightDetails(returnFlightId)).thenReturn(Optional.of(returnFlightDto));
		when(bookingRepo.save(any())).thenReturn(bookingEntity);

		String result = bookingService.bookFlight(bookingDto);

		// Assert
		assertTrue(result.contains("Round-trip Booking Successful"));
		verify(flightClient, times(2)).updateSeats(anyInt(), eq(-1));
		verify(producer, never()).sendBookingMessage(any());
	}

	@Test
	void testBookFlight_outboundFlightNotFound() {
		when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> bookingService.bookFlight(bookingDto));
		verify(flightClient, never()).updateSeats(anyInt(), anyInt());
	}

	@Test
	void testBookFlight_insufficientSeats() {

		flightDto.setTotalSeats(0);

		when(userRepo.findByEmail(any())).thenReturn(Optional.of(user));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.of(flightDto));

		assertThrows(BookingException.class, () -> bookingService.bookFlight(bookingDto));
	}

	@Test
	void testGetBookingDetails_success() {

		bookingEntity.setPassengers(List.of(new PassengerEntity()));

		when(bookingRepo.findByPnr("PNR1234")).thenReturn(Optional.of(bookingEntity));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.of(flightDto));

		BookingGetResponse result = bookingService.getBookingDetails("PNR1234");

		assertEquals("PNR1234", result.getPnr());
	}

	@Test
	void testCancelTicket_success() {

		bookingEntity.setNoOfSeats(1);
		bookingEntity.setPassengers(List.of(new PassengerEntity()));

		when(bookingRepo.findByPnr("PNR1234")).thenReturn(Optional.of(bookingEntity));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.of(flightDto));

		String result = bookingService.cancelTicket("PNR1234");

		assertTrue(result.contains("successfully cancelled"));
		verify(flightClient).updateSeats(10, 1);
	}

	@Test
	void testCancelTicket_lessThan24Hours() {

		flightDto.setDepatureTime(LocalDateTime.now().plusHours(1)); // less than 24 hours

		when(bookingRepo.findByPnr("PNR1234")).thenReturn(Optional.of(bookingEntity));
		when(flightClient.getFlightDetails(10)).thenReturn(Optional.of(flightDto));

		assertThrows(BookingException.class, () -> bookingService.cancelTicket("PNR1234"));
	}

	@Test
	void testGetHistoryByEmail_success() {

		when(bookingRepo.findAllByEmailId("virupavaraprasad@gmail.com")).thenReturn(List.of(bookingEntity));

		List<Bookingdto> result = bookingService.getHistoryByEmail("virupavaraprasad@gmail.com");

		assertEquals(1, result.size());
		assertEquals("virupavaraprasad@gmail.com", result.get(0).getEmailId());
	}

	@Test
	void getHistoryByEmail_noUserId() {
		String email = "virupavaraprasad@gmail.com";
		bookingEntity.setUserId(null);
		bookingEntity.setNoOfSeats(2);

		when(bookingRepo.findAllByEmailId(email)).thenReturn(List.of(bookingEntity));

		List<Bookingdto> result = bookingService.getHistoryByEmail(email);

		assertFalse(result.isEmpty());
		assertEquals(2, result.get(0).getNoOfSeats());
		assertNull(result.get(0).getName());

		verify(userRepo, never()).findById(anyString());
	}


}
