package com.bookingapp.producer;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.bookingapp.config.RabbitConfig;
import com.bookingapp.dto.BookingGetResponse;

@Component
public class BookingProducer {

    private final RabbitTemplate rabbitTemplate;

    public BookingProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendBookingMessage(BookingGetResponse data) {
    	data.setMessage("Booking");
        rabbitTemplate.convertAndSend( RabbitConfig.EXCHANGE_NAME,
                RabbitConfig.ROUTING_KEY, data);	
    }
    public void sendCancellationMessage(BookingGetResponse data) {
    	data.setMessage("Cancel");
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME,RabbitConfig.ROUTING_KEY,data);
    }
}
