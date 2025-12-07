package com.bookingapp.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlightDto {

	private Integer flightId;
	private Integer totalSeats;
	private LocalDateTime depatureTime;
	private String fromLocation;
    private String toLocation;
    private Double price;
    private String airlineName;
	public Integer getFlightId() {
		return flightId;
	}
	public void setFlightId(Integer flightId) {
		this.flightId = flightId;
	}
	public Integer getTotalSeats() {
		return totalSeats;
	}
	public void setAvailSeats(Integer availSeats) {
		this.totalSeats = availSeats;
	}
	public LocalDateTime getDepatureTime() {
		return depatureTime;
	}
	public void setDepatureTime(LocalDateTime depatureTime) {
		this.depatureTime = depatureTime;
	}
	public String getFromLocation() {
		return fromLocation;
	}
	public void setFromLocation(String fromLocation) {
		this.fromLocation = fromLocation;
	}
	public String getToLocation() {
		return toLocation;
	}
	public void setToLocation(String toLocation) {
		this.toLocation = toLocation;
	}
	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
	public String getAirlineName() {
		return airlineName;
	}
	public void setAirlineName(String airlineName) {
		this.airlineName = airlineName;
	}
	public void setTotalSeats(Integer totalSeats) {
		this.totalSeats = totalSeats;
	}
}
