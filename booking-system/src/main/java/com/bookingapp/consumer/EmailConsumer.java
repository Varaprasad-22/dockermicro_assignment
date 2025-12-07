package com.bookingapp.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.bookingapp.config.RabbitConfig;
import com.bookingapp.dto.BookingGetResponse;
import com.bookingapp.service.EmailService;

@Component
public class EmailConsumer {

	 private final EmailService emailService;

	    public EmailConsumer(EmailService emailService) {
	        this.emailService = emailService;
	    }
	    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
	public  void receiveBookingMessage(BookingGetResponse data) {
	    	String a=data.getMessage();
	    	if("cancel".equalsIgnoreCase(a)) {

		        String email =data.getEmail();
		        String pnr = data.getPnr();

		        emailService.sendCancellationEmail(email, pnr);
	    	}else {
		emailService.sendBookingEmail(data);}
	}
}
