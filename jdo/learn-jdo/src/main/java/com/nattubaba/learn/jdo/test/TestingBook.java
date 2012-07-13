package com.nattubaba.learn.jdo.test;

import static com.nattubaba.learn.jdo.utils.JdoUtils.logger;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import javax.jdo.Extent;
import javax.jdo.JDOCanRetryException;
import javax.jdo.PersistenceManager;
import javax.jdo.Transaction;

import com.nattubaba.learn.jdo.daos.AuthorDAO;
import com.nattubaba.learn.jdo.entities.Address;
import com.nattubaba.learn.jdo.entities.Author;
import com.nattubaba.learn.jdo.entities.Book;
import com.nattubaba.learn.jdo.entities.City;
import com.nattubaba.learn.jdo.entities.Country;
import com.nattubaba.learn.jdo.entities.Publication;
import com.nattubaba.learn.jdo.utils.JdoUtils;

public class TestingBook 
{
	private static final String NATTUBABA_PUB = "Nattubaba Pub.";
	private static final String SAMASYA_KA_SAMADHAN = "Samasya Ka Samadhan";
	private static final String NITIRAJ_SINGH_RATHORE = "nitiraj singh rathore";

	public static void testNonPersistent()
	{
		try
		{
			// at this point the Author and Publication were not persistenceCapable
			Book book = new Book("Book1", new Author(), new Publication() , new Date(), UUID.randomUUID().toString());
			Book savedBook = JdoUtils.getPMF().getPersistenceManager().makePersistent(book);
			logger.info("Saved Book : " + savedBook);
			System.out.println("Saved Book : " + savedBook);
		}
		catch( JDOCanRetryException e)
		{
			logger.error(e);
		}
	}
	
	public static void addBookToAuthor()
	{
		try
		{ 
			Country country = new Country("India", "91");
			City city = new City("Bangalore", "590026");
			Address address = new Address("Flat-404", "Kalpavriksha Appartments", "Tavarekere", city, country);
			Date date = null;
			try {
				date = new SimpleDateFormat("dd/MM/yy").parse("03/07/85");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			Author author = new Author("nitiraj singh rathore",date, "nitiraj.rathore@gmail.com",address);
			Publication publication = new Publication(NATTUBABA_PUB);
			PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
			Author author = AuthorDAO.getAuthorByName(NITIRAJ_SINGH_RATHORE);
			Book book = new Book(SAMASYA_KA_SAMADHAN, author, publication, new Date(), UUID.randomUUID().toString());
			author.addBook(book);
			publication.addBook(book);
			
			Book savedBook = pm.makePersistent(book);
			logger.info("Saved Book : " + savedBook);
			System.out.println("Saved Book : " + savedBook);
			pm.close();
		}
		catch( JDOCanRetryException e)
		{
			logger.error(e);
		}
	}
	
	public static void addBookToAuthorBySamePM()
	{
		try
		{ 
			Country country = new Country("India", "91");
			City city = new City("Bangalore", "590026");
			Address address = new Address("Flat-404", "Kalpavriksha Appartments", "Tavarekere", city, country);
			Date date = null;
			try {
				date = new SimpleDateFormat("dd/MM/yy").parse("03/07/85");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
//			Author author = new Author("nitiraj singh rathore",date, "nitiraj.rathore@gmail.com",address);
			Publication publication = new Publication(NATTUBABA_PUB);
			PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
			Transaction tx = pm.currentTransaction();
			try
			{
				tx.begin();
				Author author = AuthorDAO.getAuthorByName(pm,NITIRAJ_SINGH_RATHORE);
				Book book = new Book(SAMASYA_KA_SAMADHAN, author, publication, new Date(), UUID.randomUUID().toString());
				author.addBook(book);
				publication.addBook(book);
				
				Book savedBook = pm.makePersistent(book);
				logger.info("Saved Book : " + savedBook);
				System.out.println("Saved Book : " + savedBook);
				tx.commit();
			}
			finally
			{
				if( tx.isActive() )
					tx.rollback();
				pm.close();
			}
				
		}
		catch( JDOCanRetryException e)
		{
			logger.error(e);
		}
	}
	
	public static void addBook()
	{
		try
		{ 
			Country country = new Country("India", "91");
			City city = new City("Bangalore", "590026");
			Address address = new Address("Flat-404", "Kalpavriksha Appartments", "Tavarekere", city, country);
			Date date = null;
			try {
				date = new SimpleDateFormat("dd/MM/yy").parse("03/07/85");
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Author author = new Author(NITIRAJ_SINGH_RATHORE,date, "nitiraj.rathore@gmail.com",address);
			Publication publication = new Publication(NATTUBABA_PUB);
			PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
//			Author author = AuthorDAO.getAuthorByName("nitiraj singh rathore");
			Book book = new Book(SAMASYA_KA_SAMADHAN, author, publication, new Date(), UUID.randomUUID().toString());
			author.addBook(book);
			publication.addBook(book);
			
			Book savedBook = pm.makePersistent(book);
			logger.info("Saved Book : " + savedBook);
			System.out.println("Saved Book : " + savedBook);
			pm.close();
		}
		catch( JDOCanRetryException e)
		{
			logger.error(e);
		}
	}
	
	public static void listAuthors()
	{
		PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
		Extent<Author> extent = pm.getExtent(Author.class);
		
		Iterator<Author> authors = extent.iterator();
		while(authors.hasNext())
		{
			Author author = authors.next();
			logger.info("Author : " + author + ":: Books : " + author.getBooks());
		}
		extent.closeAll();
		pm.close();
	}
	
	public static void listSupportedOptions()
	{
		logger.info(JdoUtils.getPMF().supportedOptions());
	}
	public static void main(String argv[])
	{
//		addBook();
//		addBookToAuthorBySamePM();
//		listAuthors();
		listSupportedOptions();
	}
}
