package com.nattubaba.jerseytest.composite.test;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.nattubaba.jerseytest.composite.Address;
import com.nattubaba.jerseytest.composite.Person;
import com.nattubaba.jerseytest.composite.Tuple;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;

public class TestComposite 
{
	
	private static final String SERVICE_URL = "http://localhost:8080/jerseytest/rest";
	public static Client client = null;
	static{
		ClientConfig cc = new DefaultClientConfig();
		cc.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING,Boolean.TRUE);
		client = Client.create(cc);
	}
	
	public static void testComposite()
	{
		Person p1 = new Person("nitiraj");
		Person p2 = new Person("rituraj");
		Person p3 = new Person("rahul");
		Person p4 = new Person("pankaj");
		
		Address a1 = new Address("101","audugodi","bangalore");
		Address a2 = new Address("404","kalpavriksha","bangalore");
		Address a3 = new Address("61","vivekanand","jhabua");
		
		p1.setAddress(a2);
		p2.setAddress(a3);
		p3.setAddress(a2);
		p4.setAddress(a1);
		
		List<Person> persons = new ArrayList<Person>(4);
		persons.add(p1);
		persons.add(p2);
		persons.add(p3);
		persons.add(p4);
		
		List<Address> addresses = new ArrayList<Address>(3);
		addresses.add(a1);
		addresses.add(a2);
		addresses.add(a3);
		
		Tuple tuple = new Tuple(persons, addresses);
		String svurl = SERVICE_URL + "/composite/save";
		System.out.println("svurl : " + svurl);
		WebResource webResource = client.resource(svurl);
		ClientResponse cr = webResource.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class,tuple);
		
		if( cr.getStatus() != Status.OK.getStatusCode()  )
		{
			System.out.println("Error Occured on server");
			return;
		}
		
		Tuple savedTuple = cr.getEntity(Tuple.class);
		int i = 0 ;
		for( Address a : savedTuple.getAddress() )
		{
			System.out.println("Client Address a = " + a + ": " + System.identityHashCode(a) );
			a.setId(++i);
		}
		for(Person p : savedTuple.getPerson() )
		{
			System.out.println("Client Person p = " + p + ": " + System.identityHashCode(p) + " and Address a :" + p.getAddress() + " : " + System.identityHashCode(p.getAddress()) );
			p.setId(++i);
		}
	}
	
	public static void main(String[] args) {
		testComposite();
	}
}
