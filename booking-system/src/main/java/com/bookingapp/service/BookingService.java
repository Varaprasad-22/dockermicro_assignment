package com.bookingapp.service;

import java.util.List;

import com.bookingapp.dto.BookingGetResponse;
import com.bookingapp.dto.Bookingdto;

public interface BookingService {
	String bookFlight(Bookingdto data);

	BookingGetResponse getBookingDetails(String pnr);

	String cancelTicket(String pnr);

	List<BookingGetResponse> getHistoryByEmail(String emailId);
	
	 List<String> getAllSeats(Integer flightId);
}
