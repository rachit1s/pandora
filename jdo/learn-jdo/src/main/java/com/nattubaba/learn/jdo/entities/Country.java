package com.nattubaba.learn.jdo.entities;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.Element;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.PersistenceModifier;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Value;

@PersistenceCapable
public class Country
{
	@Persistent(primaryKey="true", valueStrategy=IdGeneratorStrategy.INCREMENT)
	private long id;
	
	private String name;
	private String countryCode;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT,  defaultFetchGroup="true" )
	@Key(types=java.lang.String.class)
	@Value(types=java.lang.String.class)
	@Join(table="properties_countries", column="countryId")
	private HashMap<String, String> properties;
	
	@Persistent(persistenceModifier=PersistenceModifier.PERSISTENT,  defaultFetchGroup="true")
	@Join(table="countries_cities", column="countryKiId")
	@Element(column="countrya_citiya")
	private Collection<City> cities;

	public Collection<City> getCities() {
		return cities;
	}
	public void setCities(Collection<City> cities) {
		this.cities = cities;
	}
	public HashMap<String, String> getProperties() {
		return properties;
	}
	public void setProperties(HashMap<String, String> properties) {
		this.properties = properties;
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
		this.cities = new HashSet<City>();
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
