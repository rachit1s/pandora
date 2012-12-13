/*
 * MailAuthenticator.java
 *
 * Created on June 22, 2006, 12:30 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package transbit.tbits.mail;
import javax.mail.*;
import javax.swing.*;
import java.util.*;

/**
 *
 * @author Vinod Gupta
 */
public class MailAuthenticator extends Authenticator{
    
    /**
     * Creates a new instance of MailAuthenticator
     */
    private String userName ;
    private String passward ;
    public MailAuthenticator(String aUsername , String aPassward) {
        userName=aUsername;
        passward=aPassward;
    }
    public PasswordAuthentication 
      getPasswordAuthentication() {
    return new PasswordAuthentication(
      userName, passward);
  }
}
