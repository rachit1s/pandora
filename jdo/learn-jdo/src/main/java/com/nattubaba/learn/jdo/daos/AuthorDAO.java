package com.nattubaba.learn.jdo.daos;

import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;

import com.nattubaba.learn.jdo.entities.Author;
import com.nattubaba.learn.jdo.utils.JdoUtils;

public class AuthorDAO 
{
	public static Author getAuthorByName(String name)
	{
		PersistenceManager pm = JdoUtils.getPMF().getPersistenceManager();
		Transaction tx = pm.currentTransaction();
		try
		{
			tx.begin();
			Query query = pm.newQuery(Author.class, "name == authorName");
			query.declareParameters("String authorName");
			Collection<Author> authors = (Collection<Author>) query.execute(name);
			if( null == authors || authors.size() == 0 )
			{
				tx.commit();
				return null;
			}
			else
			{
				Author a = authors.iterator().next();
				tx.commit();
				return a;
			}
		}
		finally
		{
			if( tx.isActive() )
				tx.rollback();
			
			pm.close();
		}
	}
	
	public static Author getAuthorByName(PersistenceManager pm, String name)
	{
		Query query = pm.newQuery(Author.class, "name == authorName");
		
		try
		{
			query.declareParameters("String authorName");
			Collection<Author> authors = (Collection<Author>) query.execute(name);
			if( null == authors || authors.size() == 0 )
			{
				return null;
			}
			else
			{
				Author a = authors.iterator().next();
				return a;
			}
		}
		finally
		{
			query.closeAll();
		}
	}
}
