package com.bookingapp.dto;

import java.util.List;

public class BookingGetResponse {
	private Integer flightId;
	private String pnr;
	private String message;
	private String email;
	private String name;
	private boolean status;
	private List<Passengers> passengersList;

	public Integer getFlightId() {
		return flightId;
	}

	public void setFlightId(Integer flightId) {
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
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean statusl) {
		this.status = statusl;
	}
}
