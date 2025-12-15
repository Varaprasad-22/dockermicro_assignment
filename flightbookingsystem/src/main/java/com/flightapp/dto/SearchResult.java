package com.flightapp.dto;

import java.util.List;
import lombok.Data;

@Data
public class SearchResult {

	private List<Flight> outboundFlights;
	private List<Flight> inboundFlights;
	private String message;

	public List<Flight> getOutboundFlights() {
		return outboundFlights;
	}

	public void setOutboundFlights(List<Flight> outboundFlights) {
		this.outboundFlights = outboundFlights;
	}

	public List<Flight> getInboundFlights() {
		return inboundFlights;
	}

	public void setInboundFlights(List<Flight> inboundFlights) {
		this.inboundFlights = inboundFlights;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}