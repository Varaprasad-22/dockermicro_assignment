package com.bookingapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.bookingapp.model.BookingEntity;

@Repository
public interface BookingRepository extends MongoRepository<BookingEntity, String>{


	List<BookingEntity> findAllByEmailId(String emailId);

	Optional<BookingEntity> findByPnr(String pnr);

	void deleteByPnr(String pnr);

	List<BookingEntity> findAllByFlightId(Integer outboundFlightId);

}
