package com.tbitsglobal.ddc.rest;

import java.util.ArrayList;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.tbitsglobal.ddc.common.DocumentNumberSet;
import com.tbitsglobal.ddc.common.DocumentSet;

@Path("/ddc-service")
public class DDCRestService {
	
	@Path("/getText")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static DocumentNumberSet getText(DocumentSet docSet)
	{
		return  new DocumentNumberSet();
	}
	
	@Path("/hello")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public static String getHelloMessage( @QueryParam("name") String name )
	{
		return "Hello " + name;
	}

}
