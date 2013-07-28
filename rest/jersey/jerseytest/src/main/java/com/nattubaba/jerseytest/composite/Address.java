package com.nattubaba.jerseytest.composite;

import java.io.Serializable;

public class Address implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -556072743423070521L;
	private int id;
	private String street;
	private String flatNo;
	private String city;
	
	public Address() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Address(String street, String flatNo, String city) {
		super();
		this.street = street;
		this.flatNo = flatNo;
		this.city = city;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getFlatNo() {
		return flatNo;
	}
	public void setFlatNo(String flatNo) {
		this.flatNo = flatNo;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	@Override
	public String toString() {
		return "Address [id=" + id + ", street=" + street + ", flatNo="
				+ flatNo + ", city=" + city + "]";
	}
}
