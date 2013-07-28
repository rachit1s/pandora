package com.nattubaba.learn.jdo.entities;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

@PersistenceCapable
//@Queries({@Query(value="select from Author where name")})
public class Author 
{
	private String name;
	private Date DOB;
	private String email;
	
	private Set<Book> books = new HashSet<Book>();
	private Address address;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDOB() {
		return DOB;
	}
	public void setDOB(Date dOB) {
		DOB = dOB;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public Collection<Book> getBooks() {
		return books;
	}
	public void setBooks(Set<Book> books) {
		this.books = books;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	public Author() {
		super();
	}
	public Author(String name, Date dOB, String email,
			Address address) {
		super();
		this.name = name;
		DOB = dOB;
		this.email = email;
		this.address = address;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
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
		Author other = (Author) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		return true;
	}
	
	public Set<Book> addBook( Book book )
	{
		this.books.add(book);
		return books;
	}
	
	public Set<Book> addBooks(Collection<Book> books)
	{
		this.books.addAll(books);
		return this.books;
	}
	
	@Override
	public String toString() {
		return "Author [name=" + name + ", DOB=" + DOB + ", email=" + email
				+", address=" + address + "]";
	}
}
