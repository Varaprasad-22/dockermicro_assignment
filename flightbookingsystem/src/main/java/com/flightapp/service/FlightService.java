package com.flightapp.service;


import org.springframework.http.ResponseEntity;

import com.flightapp.dto.Flight;
import com.flightapp.dto.Search;
import com.flightapp.dto.SearchResult;

public interface FlightService {
	int addFlight(Flight flight);
	SearchResult search(Search data);
	ResponseEntity<Flight> getById(Integer flightId);
	void updateDetails(Integer flightId, Integer changeInSeats);
}
