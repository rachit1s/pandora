
package transbit.tbits.common;
import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

// Authentication for Secure password .

 public class TransbitAuthenticator extends Authenticator
  { 
	  String user;
	  String pass;
	  public TransbitAuthenticator(String login, String password)
	  {
		  this.user = login;
		  this.pass = password;
		
	  }
  public PasswordAuthentication getPasswordAuthentication()
	  {
			return new PasswordAuthentication(user, pass);
       }
  }