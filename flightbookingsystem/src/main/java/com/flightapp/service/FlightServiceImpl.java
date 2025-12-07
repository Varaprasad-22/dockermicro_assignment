package com.flightapp.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.flightapp.dto.Flight;
import com.flightapp.model.Airline;
import com.flightapp.model.FlightEntity;
import com.flightapp.dto.Search;
import com.flightapp.dto.SearchResult;
import com.flightapp.exceptions.ResourceNotFoundException;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.repository.FlightRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;

@Service
public class FlightServiceImpl implements FlightService {

	private final FlightRepository flightRepository;
	private final AirlineRepository airlineRepository;

	public FlightServiceImpl(FlightRepository flightRepository, AirlineRepository airlineRepository) {
		this.flightRepository = flightRepository;
		this.airlineRepository = airlineRepository;
	}

	@Override
//	@CircuitBreaker(name="flightServiceCb",fallbackMethod = "addFlightCb")
	public int addFlight(Flight flightRequest) {
		try {
			FlightEntity flightEntity = new FlightEntity();
			Optional<Airline> airlines = airlineRepository.findByAirlineName(flightRequest.getAirlineName());
			if (!airlines.isPresent()) {
				throw new ResourceNotFoundException(
						"Flight save failed: Airline '" + flightRequest.getAirlineName() + "' not found.");
			}
			Optional<FlightEntity> flightOpt = flightRepository.findByFlightNumber(flightRequest.getFlightNumber());
			if (flightOpt.isPresent()) {
				throw new ResourceNotFoundException(
						"Flight with number " + flightRequest.getFlightNumber() + " does  exist.");
			}
			Airline airline = airlines.get();
			flightEntity.setAirlineId(airline.getAirlineId());
			flightEntity.setFlightNumber(flightRequest.getFlightNumber());
			flightEntity.setFromLocation(flightRequest.getFromPlace());
			flightEntity.setToLocation(flightRequest.getToPlace());
			flightEntity.setPrice(flightRequest.getPrice());
			flightEntity.setArrivalTime(flightRequest.getArrivalTime());
			flightEntity.setDepatureTime(flightRequest.getDepatureTime());
			flightEntity.setTotalSeats(flightRequest.getTotalSeats());
			flightEntity.setAvaliSeats(flightRequest.getTotalSeats());
			FlightEntity savedFlight = flightRepository.save(flightEntity);
			return savedFlight.getFlightId();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Flight Saved failed: " + e.getMessage());
		}

	}

	public int addFlightCb(Flight flightRequest, Throwable ex) {
		return -1;
	}

	@Override
	@CircuitBreaker(name = "flightServiceCb", fallbackMethod = "SearchCb")
	public SearchResult search(Search searchRequest) {
		SearchResult result = new SearchResult();

		LocalDateTime startOfDay = searchRequest.getDepartureDate().atStartOfDay();
		LocalDateTime endOfDay = searchRequest.getDepartureDate().atTime(LocalTime.MAX);

		List<FlightEntity> outboundEntities = flightRepository.findByFromLocationAndToLocationAndDepatureTimeBetween(
				searchRequest.getFromPlace(), searchRequest.getToPlace(), startOfDay, endOfDay);
		result.setOutboundFlights(mapEntitiesToDTOs(outboundEntities));
		if (outboundEntities.isEmpty()) {
			throw new ResourceNotFoundException("No flights found");
		}

		if ("round-trip".equalsIgnoreCase(searchRequest.getTripType()) && searchRequest.getReturnDate() != null) {

			LocalDateTime returnStart = searchRequest.getReturnDate().atStartOfDay();
			LocalDateTime returnEnd = searchRequest.getReturnDate().atTime(LocalTime.MAX);

			List<FlightEntity> inboundEntities = flightRepository.findByFromLocationAndToLocationAndDepatureTimeBetween(
					searchRequest.getToPlace(), searchRequest.getFromPlace(), returnStart, returnEnd);
			if (inboundEntities.isEmpty()) {
				throw new ResourceNotFoundException("No return flights found");
			}
			result.setInboundFlights(mapEntitiesToDTOs(inboundEntities));
		}

		return result;
	}

	public SearchResult SearchCb() {

		SearchResult searchCb = new SearchResult();
		searchCb.setMessage("Failed Server Flight Try Later On");
		return null;
	}

	private List<Flight> mapEntitiesToDTOs(List<FlightEntity> flightEntities) {
		return flightEntities.stream().map(entity -> {
			Flight flightRequestDto = new Flight();
			Optional<Airline> airlines = airlineRepository.findByAirlineId(entity.getAirlineId());
			if (!airlines.isPresent()) {
				throw new ResourceNotFoundException("Airline not found for ID: " + entity.getAirlineId());
			}
			Airline airline = airlines.get();
			flightRequestDto.setAirlineName(airline.getAirlineName());
			flightRequestDto.setFlightNumber(entity.getFlightNumber());
			flightRequestDto.setFromPlace(entity.getFromLocation());
			flightRequestDto.setToPlace(entity.getToLocation());
			flightRequestDto.setPrice(entity.getPrice());
			flightRequestDto.setTotalSeats(entity.getTotalSeats());
			flightRequestDto.setDepatureTime(entity.getDepatureTime());
			flightRequestDto.setArrivalTime(entity.getArrivalTime());

			return flightRequestDto;
		}).collect(Collectors.toList());
	}

	@Override
	public ResponseEntity<Flight> getById(Integer flightId) {
		Optional<FlightEntity> flightdata = flightRepository.findById(flightId);
		if (flightdata.isEmpty()) {
			throw new ResourceNotFoundException("No flight with that id");
		}
		FlightEntity entity = flightdata.get();
		Flight dto = new Flight();
		Optional<Airline> air = airlineRepository.findByAirlineId(entity.getAirlineId());
		if (air.isEmpty()) {
			throw new ResourceNotFoundException("No airline with that id");
		}
		Airline air1 = air.get();
		dto.setAirlineName(air1.getAirlineName());
		dto.setFlightId(flightId);
		dto.setFlightNumber(entity.getFlightNumber());
		dto.setFromPlace(entity.getFromLocation());
		dto.setArrivalTime(entity.getArrivalTime());
		dto.setDepatureTime(entity.getDepatureTime());
		dto.setToPlace(entity.getToLocation());
		dto.setTotalSeats(entity.getAvaliSeats());
		dto.setPrice(entity.getPrice());
		return ResponseEntity.ok(dto);
	}

	@Override
	@Transactional
	public void updateDetails(Integer flightId, Integer changeInSeats) {
		Optional<FlightEntity> flightdata = flightRepository.findById(flightId);
		if (flightdata.isEmpty()) {
			throw new ResourceNotFoundException("No flight with that id");
		}
		FlightEntity entity = flightdata.get();
		entity.setAvaliSeats(entity.getAvaliSeats() + changeInSeats);
		flightRepository.save(entity);
		return;
	}

}
