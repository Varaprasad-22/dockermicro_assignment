package com.bookingapp;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
@EnableDiscoveryClient
@EnableRabbit
public class BookingSystemApplication {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SpringApplication.run(BookingSystemApplication.class,args);
		
	}

}
