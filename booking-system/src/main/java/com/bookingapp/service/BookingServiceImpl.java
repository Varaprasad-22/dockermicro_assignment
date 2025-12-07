package com.bookingapp.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.bookingapp.client.FlightServiceClient;
import com.bookingapp.dto.BookingGetResponse;
import com.bookingapp.dto.Bookingdto;
import com.bookingapp.dto.FlightDto;
import com.bookingapp.dto.Passengers;
import com.bookingapp.exceptions.BookingException;
import com.bookingapp.exceptions.ResourceNotFoundException;
import com.bookingapp.model.BookingEntity;
import com.bookingapp.model.PassengerEntity;
import com.bookingapp.model.User;
import com.bookingapp.producer.BookingProducer;
import com.bookingapp.repository.BookingRepository;
import com.bookingapp.repository.UserRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class BookingServiceImpl implements BookingService {
	private final BookingRepository bookingRepository;
	private final UserRepository userRepository;
	private final FlightServiceClient flightserviceclient;
	private final BookingProducer producer;

	public BookingServiceImpl(BookingRepository bookingRepository, UserRepository userRepository,
			FlightServiceClient flightserviceclient, BookingProducer producer) {
		this.bookingRepository = bookingRepository;
		this.userRepository = userRepository;
		this.flightserviceclient = flightserviceclient;
		this.producer = producer;
	}

	@Override
	@CircuitBreaker(name = "BookingServiceCb", fallbackMethod = "boooking")
	public String bookFlight(Bookingdto data) {

		if(data.getPassengers().size()!=data.getNoOfSeats()) {
			throw new BookingException("No Of Seats and tickets didn't match");
		}
		User user = getOrCreateUser(data.getEmailId(), data.getName());
		String userId = user.getUserId();

		int outboundId = data.getOutboundFlightId();

		FlightDto outboundFlight = flightserviceclient.getFlightDetails(data.getOutboundFlightId())
				.orElseThrow(() -> new ResourceNotFoundException("Outbound flight not found"));

		if (outboundFlight.getTotalSeats() < data.getNoOfSeats()) {
			throw new BookingException("Not enough seats in outbound flight.");
		}
		if(LocalDateTime.now().isAfter(outboundFlight.getDepatureTime())||LocalDateTime.now().isEqual(outboundFlight.getDepatureTime())) {
			throw new BookingException("Flight Already Depatured");
		}
		BookingEntity bookingEntity = new BookingEntity();
		bookingEntity.setUserId(userId);
		bookingEntity.setEmailId(data.getEmailId());
		bookingEntity.setFlightId(data.getOutboundFlightId());
		bookingEntity.setNoOfSeats(data.getNoOfSeats());
		bookingEntity.setStatus(true);
		bookingEntity.setBookingTime(LocalDateTime.now());
		String pnr=(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
		bookingEntity.setPnr(pnr);
		data.getPassengers().forEach(passengerRequest -> {
			PassengerEntity passengerEntity = new PassengerEntity();
			passengerEntity.setName(passengerRequest.getName());
			passengerEntity.setAge(passengerRequest.getAge());
			passengerEntity.setGender(passengerRequest.getGender());
			passengerEntity.setMeal(passengerRequest.getMeal());
			passengerEntity.setSeatNo(passengerRequest.getSeatNo());
			bookingEntity.addPassenger(passengerEntity);
		});
		List<BookingEntity> existingdata=bookingRepository.findAllByFlightId(data.getOutboundFlightId());
		for(BookingEntity exis:existingdata) {
			for(PassengerEntity passengersFromDb:exis.getPassengers()) {
				for(Passengers passenger:data.getPassengers()) {
					if(passengersFromDb.getSeatNo().equalsIgnoreCase(passenger.getSeatNo())) {
						throw new BookingException("Seat "+ passenger.getSeatNo()+" is alredy booked");
					}
				}
			}
		}
		flightserviceclient.updateSeats(data.getOutboundFlightId(), data.getNoOfSeats() * -1);

		Integer returnId = data.getReturnFlightId();
		if (returnId != null) {

			FlightDto returnFlight = flightserviceclient.getFlightDetails(returnId)
					.orElseThrow(() -> new ResourceNotFoundException("Return flight not found"));

			if (returnFlight.getTotalSeats() < data.getNoOfSeats()) {

				flightserviceclient.updateSeats(data.getOutboundFlightId(), data.getNoOfSeats());
				throw new BookingException("Not enough seats in return flight.");
			}

			bookingEntity.setReturnFlight(returnId);
			flightserviceclient.updateSeats(data.getOutboundFlightId(), data.getNoOfSeats() * -1);

			bookingRepository.save(bookingEntity);

			return "Round-trip Booking Successful! PNR: " + bookingEntity.getPnr();
		}
		
		bookingRepository.save(bookingEntity);
		BookingGetResponse datarabbitMq=getBookingDetails(pnr);
		producer.sendBookingMessage(datarabbitMq);

		return "One-way Booking Successful! PNR: " + bookingEntity.getPnr();
	}

	public ResponseEntity<String> boooking(Bookingdto data, Throwable ex) {
		return ResponseEntity.status(503).body("Booking Service is DOWN. Try again later.");
	}

	private User getOrCreateUser(String email, String name) {
		Optional<User> userOpt = userRepository.findByEmail(email);

		if (userOpt.isPresent()) {
			return userOpt.get();
		} else {
			User newUser = new User();
			newUser.setEmail(email);
			newUser.setName(name);
			return userRepository.save(newUser);
		}
	}

	@Override
	@CircuitBreaker(name = "BookingServiceCb", fallbackMethod = "ByPnr")
	public BookingGetResponse getBookingDetails(String pnr) {
		Optional<BookingEntity> bookingOpt = bookingRepository.findByPnr(pnr);
		if (!bookingOpt.isPresent()) {
			throw new ResourceNotFoundException("No booking found with PNR: " + pnr);
		}

		BookingEntity bookingEntity = bookingOpt.get();
		FlightDto flightDetails = flightserviceclient.getFlightDetails((bookingEntity.getFlightId())).orElse(null);
		BookingGetResponse response = new BookingGetResponse();
		response.setPnr(bookingEntity.getPnr());
		response.setFlightId(String.valueOf(bookingEntity.getFlightId()));
		List<Passengers> passengersList = bookingEntity.getPassengers().stream().map(entity -> {
			Passengers passengerDto = new Passengers();
			passengerDto.setName(entity.getName());
			passengerDto.setAge(entity.getAge());
			passengerDto.setGender(entity.getGender());
			passengerDto.setMeal(entity.getMeal());
			passengerDto.setSeatNo(entity.getSeatNo());
			return passengerDto;
		}).collect(Collectors.toList());
response.setEmail(bookingEntity.getEmailId());
		response.setPassengersList(passengersList);
		return response;
	}

	public BookingGetResponse ByPnr(String pnr, Throwable ex) {
		BookingGetResponse response = new BookingGetResponse();
		response.setPnr(null);
		response.setMessage("failed to Send Request Server Down");
		response.setFlightId(null);
		return response;
	}

	@Override
//	@CircuitBreaker(name = "BookingServiceCb", fallbackMethod = "cancleTicketCb")
	public String cancelTicket(String pnr) {
		BookingEntity bookingOpt = bookingRepository.findByPnr(pnr).orElseThrow(()-> new ResourceNotFoundException("Cancellation Failed: PNR not found."));
//		if (!bookingOpt.isPresent()) {
//			throw new ResourceNotFoundException("Cancellation Failed: PNR not found.");
//		}
		BookingEntity bookingEntity = bookingOpt;
		FlightDto flightDetails = flightserviceclient.getFlightDetails(bookingEntity.getFlightId())
				.orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
		LocalDateTime departureTime = flightDetails.getDepatureTime();
		long hoursRemaining = Duration.between(LocalDateTime.now(), departureTime).toHours();
		if(!bookingEntity.isStatus()) {
			throw new BookingException("Failed Since Flight booking already cancled");
		}
		if (hoursRemaining < 24) {
			throw new BookingException(
					"Cancellation Failed: Cannot cancel ticket less than 24 hours before journey date.");
		}
		flightserviceclient.updateSeats(bookingEntity.getFlightId(), bookingEntity.getNoOfSeats());
		bookingEntity.setStatus(false);
		bookingRepository.save(bookingEntity);
		BookingGetResponse datarabbitMq=getBookingDetails(pnr);
		 producer.sendCancellationMessage(datarabbitMq);
		return "Ticket with PNR " + pnr + " successfully cancelled.";
	}

	public String cancleTicketCb(String pnr, Throwable ex) {
		return "The server is Down";
	}

	@Override
	@CircuitBreaker(name = "BookingServiceCb", fallbackMethod = "ByEmail")
	public List<Bookingdto> getHistoryByEmail(String emailId) {
		List<Bookingdto> bookingData = new ArrayList<>();
		try {
			List<BookingEntity> bookingEntityList = bookingRepository.findAllByEmailId(emailId);
			if (bookingEntityList.isEmpty()) {
				throw new ResourceNotFoundException("No booking history found for email: " + emailId);
			}
			for (BookingEntity booking : bookingEntityList) {

				Bookingdto bookingDto = new Bookingdto();
				bookingDto.setEmailId(booking.getEmailId());
				bookingDto.setNoOfSeats(booking.getNoOfSeats());
				if (booking.getUserId() != null) {
					User user = new User();
					Optional<User> name = userRepository.findById(booking.getUserId());

					bookingDto.setName(name.get().getName());
				}
				List<Passengers> passengerDtoList = new ArrayList<>();
				for (PassengerEntity passengerEntity : booking.getPassengers()) {

					Passengers passengerDto = new Passengers();

					passengerDto.setName(passengerEntity.getName());
					passengerDto.setAge(passengerEntity.getAge());
					passengerDto.setGender(passengerEntity.getGender());
					passengerDto.setMeal(passengerEntity.getMeal());

					passengerDto.setSeatNo(String.valueOf(passengerEntity.getSeatNo()));

					passengerDtoList.add(passengerDto);
				}
				bookingDto.setPassengers(passengerDtoList);
				bookingData.add(bookingDto);

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bookingData;
	}

	public List<Bookingdto> ByEmail(String Email, Throwable ex) {
		Bookingdto forCb = new Bookingdto();
		forCb.setEmailId(Email);
		forCb.setName("The Server is Down Failed to Load");

		return List.of(forCb);
	}

}
