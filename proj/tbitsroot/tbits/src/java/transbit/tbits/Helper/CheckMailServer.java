/*
 * This class checks the login and username of a USer against a server on a particular port with particular protocol.
 * This protocol can be pop3,pop3s,smtp,smtps etc.
 * A dummySSLSocketFactory is used to avoid the certificate confirmation.
  */


package transbit.tbits.Helper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.AuthenticationFailedException;
import javax.mail.Store;

import java.util.Properties;

import java.io.IOException;
import java.lang.IllegalStateException;
import java.io.PrintWriter;
import java.io.IOException;
import transbit.tbits.mail.MailAuthenticator;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.exception.TBitsException;

/**
 * 
 * @author Abhishek Agarwal
 *
 */

public class CheckMailServer extends HttpServlet{
	public void doPost(HttpServletRequest aRequest,HttpServletResponse aResponse) throws ServletException,IOException {
			String emailServer = aRequest.getParameter("emailserver");
			String login = aRequest.getParameter("login");
			String password = aRequest.getParameter("password");
			String port = aRequest.getParameter("port");
			String protocol = aRequest.getParameter("protocol");
						
		//If no port is given, assume it to be 110	
			if((port == null) || port == "") {
		//Default port = 110
				port = "110";
				if(protocol.equals("pop3s"))
					port = "995";
			}
				
			
		// If User Name or password is null , assume that mail server does not require authentication
			PrintWriter out = aResponse.getWriter();
							
		try {	
			connectToServer(emailServer,port,login,password,protocol);
			}
		catch(TBitsException e){
			out.print("[{\"active\":false,\"ErrorMessage\":\""+e.getDescription()+"\"}]");
			return;
		}
					
		out.print("[{\"active\":true,\"ErrorMessage\":"+null+"}]");			
	}
	
		
	public void doGet(HttpServletRequest aRequest,HttpServletResponse aResponse) throws ServletException,IOException {
		doPost(aRequest,aResponse);
	}
	
	public void connectToServer(String emailServer,String port,String login,String password,String protocol) throws TBitsException {
		
		if((emailServer.equals("")) || (emailServer == null))
			throw new TBitsException("Please Fill in the Name of Email Server");

		if((login == null) || (login.equals(""))) 
			throw new TBitsException("Fill in the Username");
			
		if((password == null) || (password.equals(""))) 
			throw new TBitsException("Fill in the Password");
			
		
		Properties prop = PropertiesHandler.getAppAndSysProperties();
		
		prop.setProperty("mail."+protocol+".host", emailServer);
		prop.setProperty("mail."+protocol+".port",port);
		
		MailAuthenticator passwordAuthentication = new MailAuthenticator(login,password);
				
	
		try {	
		Session session = Session.getInstance(prop,passwordAuthentication);
		Store store = session.getStore(protocol);
		store.connect();
		}
		
		catch(AuthenticationFailedException a) {
			throw new TBitsException("Authentication Failed");
		}
		catch(MessagingException m) {
			throw new TBitsException("Could not connect to Mail Server");
		}
		catch(IllegalStateException i) {
			throw new TBitsException("Already Connected to Server");
		}
		catch(Exception e) {
			throw new TBitsException("Could Not Connect To Server. Some Error Occured");
		}
		return;
	}
}
