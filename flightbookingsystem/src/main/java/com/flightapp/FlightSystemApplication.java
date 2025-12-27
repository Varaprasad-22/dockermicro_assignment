package com.flightapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableDiscoveryClient
//@EnableJpaAuditing
public class FlightSystemApplication {

	public static void main(String []args) {
		SpringApplication.run(FlightSystemApplication.class,args);
	}
}
