package com.nattubaba.learn.jdo.entities;

import java.io.Serializable;
import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;

import com.nattubaba.learn.jdo.entities.Author;
import com.nattubaba.learn.jdo.entities.Publication;

@PersistenceCapable
public class Book implements Serializable
{
	private static final long serialVersionUID = 4858819908743032400L;
	private String name;
	
	private Author author;
	private Publication publication;
	private Date dateOfPublication;
	@Unique
	@Persistent( nullValue=NullValue.EXCEPTION)
	private String ISBN;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ISBN == null) ? 0 : ISBN.hashCode());
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
		Book other = (Book) obj;
		if (ISBN == null) {
			if (other.ISBN != null)
				return false;
		} else if (!ISBN.equals(other.ISBN))
			return false;
		return true;
	}
	
	private Book() {
		super();
	}
	
	public Book(String name, Author author, Publication publication,
			Date dateOfPublication, String iSBN) {
		super();
		this.name = name;
		this.author = author;
		this.publication = publication;
		this.dateOfPublication = dateOfPublication;
		ISBN = iSBN;
	}
	public Date getDateOfPublication() {
		return dateOfPublication;
	}
	public void setDateOfPublication(Date dateOfPublication) {
		this.dateOfPublication = dateOfPublication;
	}
	public String getISBN() {
		return ISBN;
	}
	public void setISBN(String iSBN) {
		ISBN = iSBN;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Author getAuthor() {
		return author;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	public Publication getPublication() {
		return publication;
	}
	public void setPublication(Publication publication) {
		this.publication = publication;
	}
	@Override
	public String toString() {
		return "Book [name=" + name + ", author=" + author + ", publication="
				+ publication + ", ISBN=" + ISBN + "]";
	}
}
