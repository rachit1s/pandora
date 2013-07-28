package com.nattubaba.jerseytest.jsontest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/todo")
public class TodoResource {
	// This method is called if XMLis request
	@GET
	@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
	public Todo getXML() {
		Todo todo = new Todo();
		todo.setSummary("This is my first todo MediaType.APPLICATION_XML");
		todo.setDescription("This is my first todo MediaType.APPLICATION_XML");
		return todo;
	}
	
	// This can be used to test the integration with the browser
	@GET
	@Produces({ MediaType.TEXT_XML })
	public Todo getHTML() {
		Todo todo = new Todo();
		todo.setSummary("This is my first todo MediaType.TEXT_XML");
		todo.setDescription("This is my first todo MediaType.TEXT_XML");
		return todo;
	}

}