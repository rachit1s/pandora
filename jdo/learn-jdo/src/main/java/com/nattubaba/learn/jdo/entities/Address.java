package com.nattubaba.learn.jdo.entities;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Address 
{
	private String houseNo;
	private String appartmentName;
	private String streetName;
	private City city;
	private Country country;
	public String getHouseNo() {
		return houseNo;
	}
	public void setHouseNo(String houseNo) {
		this.houseNo = houseNo;
	}
	public String getAppartmentName() {
		return appartmentName;
	}
	public void setAppartmentName(String appartmentName) {
		this.appartmentName = appartmentName;
	}
	public String getStreetName() {
		return streetName;
	}
	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}
	public City getCity() {
		return city;
	}
	public void setCity(City city) {
		this.city = city;
	}
	public Country getCountry() {
		return country;
	}
	public void setCountry(Country country) {
		this.country = country;
	}
	public Address(String houseNo, String appartmentName, String streetName,
			City city, Country country) {
		super();
		this.houseNo = houseNo;
		this.appartmentName = appartmentName;
		this.streetName = streetName;
		this.city = city;
		this.country = country;
	}
	public Address() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((appartmentName == null) ? 0 : appartmentName.hashCode());
		result = prime * result + ((city == null) ? 0 : city.hashCode());
		result = prime * result + ((country == null) ? 0 : country.hashCode());
		result = prime * result + ((houseNo == null) ? 0 : houseNo.hashCode());
		result = prime * result
				+ ((streetName == null) ? 0 : streetName.hashCode());
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
		Address other = (Address) obj;
		if (appartmentName == null) {
			if (other.appartmentName != null)
				return false;
		} else if (!appartmentName.equals(other.appartmentName))
			return false;
		if (city == null) {
			if (other.city != null)
				return false;
		} else if (!city.equals(other.city))
			return false;
		if (country == null) {
			if (other.country != null)
				return false;
		} else if (!country.equals(other.country))
			return false;
		if (houseNo == null) {
			if (other.houseNo != null)
				return false;
		} else if (!houseNo.equals(other.houseNo))
			return false;
		if (streetName == null) {
			if (other.streetName != null)
				return false;
		} else if (!streetName.equals(other.streetName))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Address [houseNo=" + houseNo + ", appartmentName="
				+ appartmentName + ", streetName=" + streetName + ", city="
				+ city + ", country=" + country + "]";
	}
}
