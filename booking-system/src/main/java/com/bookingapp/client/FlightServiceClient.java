package com.bookingapp.client;

import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.bookingapp.dto.FlightDto;
@FeignClient(name="flight-service")
public interface FlightServiceClient {

	@GetMapping("/api/flights/{flightId}")
    Optional<FlightDto> getFlightDetails(@PathVariable("flightId") Integer flightId);
	
	@PutMapping("/api/flights/inventory/updateSeats")
    void updateSeats(@RequestParam("flightId") Integer flightId, 
                     @RequestParam("changeInSeats") Integer changeInSeats);
}
