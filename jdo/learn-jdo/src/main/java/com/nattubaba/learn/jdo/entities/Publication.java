package com.nattubaba.learn.jdo.entities;

import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Publication 
{
	private String name;
	private Set<Book> publishedBooks = new HashSet<Book>();
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Book> getPublishedBooks() {
		return publishedBooks;
	}
	public void setPublishedBooks(Set<Book> publishedBooks) {
		this.publishedBooks = publishedBooks;
	}
	public Publication(String name) {
		super();
		this.name = name;
	}
	public Publication() {
		super();
		// TODO Auto-generated constructor stub
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		Publication other = (Publication) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public Set<Book> addBook( Book book)
	{
		this.publishedBooks.add(book);
		return this.publishedBooks;
	}
	@Override
	public String toString() {
		return "Publication [name=" + name +"]";
	}
}
