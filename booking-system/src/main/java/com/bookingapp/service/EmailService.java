package com.bookingapp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.bookingapp.dto.BookingGetResponse;

@Service
public class EmailService {
	 private final JavaMailSender mailSender;

	    public EmailService(JavaMailSender mailSender) {
	        this.mailSender = mailSender;
	    }

	    public void sendBookingEmail(BookingGetResponse booking) {

	        SimpleMailMessage message = new SimpleMailMessage();
	        message.setTo(booking.getEmail());
	        message.setSubject("Booking Confirmation: " + booking.getPnr());

	        String body = 
	                "Hello User flightId is" + booking.getFlightId() + ",\n\n"
	              + "Your booking is confirmed!\n"
	              + "Booking ID: " + booking.getPnr() + "\n"
	              + "Passengers are: ₹" + booking.getPassengersList()+ "\n\n"
	              + "Thank you for booking with us!"+booking;

	        message.setText(body);

	        mailSender.send(message);
	   }
	    public void sendCancellationEmail(String email,String pnr) {

	        String body ="Hello User Your Booking Cancelled No Refund Will be provided";

	        SimpleMailMessage msg = new SimpleMailMessage();
	        msg.setTo(email);
	        msg.setSubject("Booking Cancelled: " + pnr);
	        msg.setText(body);

	        mailSender.send(msg);
	    }
}
