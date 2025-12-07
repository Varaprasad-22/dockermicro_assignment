package com.flightapp.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.Flight;
import com.flightapp.dto.Search;
import com.flightapp.dto.SearchResult;
import com.flightapp.service.FlightService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flights")
public class FlightController {
	private final FlightService flightService; 

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

	@PostMapping("/airline/inventory")
	public ResponseEntity<Integer> addFlight(@Valid @RequestBody Flight flightEntry){
		int flightId=flightService.addFlight(flightEntry);
		return ResponseEntity.status(HttpStatus.CREATED).body(flightId);
	}
	@PostMapping("/search")
	public SearchResult searchFlights(@Valid @RequestBody Search data) {
		return flightService.search(data);
	}
	
	@GetMapping("/{flightId}")
	public ResponseEntity<Flight> getFlightDetails(@PathVariable Integer flightId){
		return flightService.getById(flightId);
	}
	@PutMapping("/inventory/updateSeats")
	public ResponseEntity<Void> updateSeats(@RequestParam("flightId") Integer flightId, 
                     @RequestParam("changeInSeats") Integer changeInSeats) {
		flightService.updateDetails(flightId,changeInSeats);
		return ResponseEntity.noContent().build();
	}
}
