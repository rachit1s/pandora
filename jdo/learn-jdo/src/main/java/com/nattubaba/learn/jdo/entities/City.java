package com.nattubaba.learn.jdo.entities;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class City 
{
	private String name;
	private String pin;
	private Country country;
	
	public City(String name, String pin, Country country) {
		super();
		this.name = name;
		this.pin = pin;
		this.country = country;
	}
	public Country getCountry() {
		return country;
	}
	public void setCountry(Country country) {
		this.country = country;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public City() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((pin == null) ? 0 : pin.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		City other = (City) obj;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (pin == null) {
			if (other.pin != null)
				return false;
		} else if (!pin.equals(other.pin))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "City [name=" + name + ", pin=" + pin + ", country=" + country
				+ "]";
	}
}
