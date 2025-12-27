package com.bookingapp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.bookingapp.dto.BookingGetResponse;
import com.bookingapp.dto.Passengers;

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
	                "Dear Passenger"+"\n "
	              +"Your flightId is " + booking.getFlightId() + ",\n\n"
	              + "Your booking is confirmed!\n"
	              + "Booking ID: " + booking.getPnr() + "\n"
	              + "Passengers are: ₹" + "\n\n";
	        String Passengers="";      
	        for(Passengers passenger:booking.getPassengersList()) {
	            	  Passengers+=
	            			  "Name : "+ passenger.getName()
	            			  +", Age : "+passenger.getAge()
	            			  +", Gender : "+passenger.getGender()
	            			  +", Meal Type : "+passenger.getMeal()
	            			  +", Seat No : "+passenger.getSeatNo()+"\n";
	              }
	        body+=Passengers;
	              body+= "Thank you for booking with us!";

	        message.setText(body);

	        mailSender.send(message);
	   }
	    public void sendCancellationEmail(String email,String pnr) {

	        String body ="Dear User, \n Your Booking has been Cancelled Succesfully \n No Refund Will be provided";

	        SimpleMailMessage msg = new SimpleMailMessage();
	        msg.setTo(email);
	        msg.setSubject("Booking Cancelled: " + pnr);
	        msg.setText(body);

	        mailSender.send(msg);
	    }
}
