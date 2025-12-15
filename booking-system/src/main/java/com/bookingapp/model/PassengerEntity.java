package com.bookingapp.model;

import org.springframework.data.mongodb.core.mapping.Document;


@Document(collection="passengers")
public class PassengerEntity {
	private int passengerId;
	private String name;
	private String gender;
	private int age;
	private String meal;
    
	private String seatNo;

	
	public int getPassengerId() {
		return passengerId;
	}

	

	public String getName() {
		return name;
	}

	public String getGender() {
		return gender;
	}

	public int getAge() {
		return age;
	}

	public String getMeal() {
		return meal;
	}

	public String getSeatNo() {
		return seatNo;
	}

	
	public void setPassengerId(int passengerId) {
		this.passengerId = passengerId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public void setMeal(String meal) {
		this.meal = meal;
	}

	public void setSeatNo(String seatNo) {
		this.seatNo = seatNo;
	}
}
