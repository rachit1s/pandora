package com.nattubaba.jerseytest.composite;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/composite")
public class CompositeService 
{
	@Path("/save")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public static Tuple savePersonAddress(Tuple tuple)
	{
		List<Person> persons = tuple.getPerson();
		List<Address> address = tuple.getAddress();
		
		int i = 0 ;
		for( Address a : address )
		{
			System.out.println("Server Address a = " + a + ": " + System.identityHashCode(a) );
			a.setId(++i);
		}
		for(Person p : persons )
		{
			System.out.println("Server Person p = " + p + ": " + System.identityHashCode(p) + " and Address a :" + p.getAddress() + " : " + System.identityHashCode(p.getAddress()) );
			p.setId(++i);
		}
		
		return new Tuple(persons, address);
	}
}
