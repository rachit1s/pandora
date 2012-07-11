package com.nattubaba.learn.jdo.entities;

import java.util.Collection;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Country
{
	private String name;
	private String countryCode;
	
	private Collection<City> cities;
	
	public Collection<City> getCities() {
		return cities;
	}
	public void setCities(Collection<City> cities) {
		this.cities = cities;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountryCode() {
		return countryCode;
	}
	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}
	public Country(String name, String countryCode) {
		super();
		this.name = name;
		this.countryCode = countryCode;
	}
	public Country() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((countryCode == null) ? 0 : countryCode.hashCode());
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
		Country other = (Country) obj;
		if (countryCode == null) {
			if (other.countryCode != null)
				return false;
		} else if (!countryCode.equals(other.countryCode))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Country [name=" + name + ", countryCode=" + countryCode + "]";
	}
}
