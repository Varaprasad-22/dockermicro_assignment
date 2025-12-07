package com.bookingapp.dto;

import java.util.List;

public class BookingGetResponse {
	private String flightId;
	private String pnr;
	private String message;
	private String Email;
	private List<Passengers> passengersList;

	public String getFlightId() {
		return flightId;
	}

	public void setFlightId(String flightId) {
		this.flightId = flightId;
	}

	public String getPnr() {
		return pnr;
	}

	public void setPnr(String pnr) {
		this.pnr = pnr;
	}

	public List<Passengers> getPassengersList() {
		return passengersList;
	}

	public void setPassengersList(List<Passengers> passengersList) {
		this.passengersList = passengersList;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getEmail() {
		return Email;
	}

	public void setEmail(String email) {
		Email = email;
	}
}
