package com.nattubaba.jerseytest.composite;

import java.io.Serializable;
import java.util.List;

public class Tuple implements Serializable
{
	List<Person> person;
	List<Address> address;
	
	public Tuple() {
		super();
		// TODO Auto-generated constructor stub
	}
	public Tuple(List<Person> person, List<Address> address) {
		super();
		this.person = person;
		this.address = address;
	}
	public List<Person> getPerson() {
		return person;
	}
	public void setPerson(List<Person> person) {
		this.person = person;
	}
	public List<Address> getAddress() {
		return address;
	}
	public void setAddress(List<Address> address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "Tuple [person=" + person + ", address=" + address + "]";
	}
}
