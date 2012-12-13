
package transbit.tbits.sms;


import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsPropEnum;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import static transbit.tbits.Helper.TBitsConstants.PKG_CONFIG;


/**
 * User: Manish
 * Date: Jun 6, 2007
 * Time: 7:31:33 PM
 */
public class SMSDevice {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_CONFIG);
    Hashtable<String,String> messageMobile;
    public SMSDevice() {
      messageMobile = new Hashtable<String, String>();
    }

public String formatGatewayString(String templateUrl, String message, String cellNo ){
    String smsGatewayString = "";
    smsGatewayString = templateUrl.replaceAll("\\$MESSAGE\\$", message);
    return smsGatewayString.replaceAll("\\$CELLNO\\$", cellNo);
}

    public boolean isSuccessful(String gateWayResponse){
        String gateWayResponsePattern = PropertiesHandler.getProperty("transbit.tbits.responsepattern");
        return gateWayResponse.matches(gateWayResponsePattern);
    }

    public boolean doSms(String cellNo,String message){
    	System.out.println("Sending SMS:" + cellNo + ":" + message);
        String templateUrl = PropertiesHandler.getProperty("transbit.tbits.smsgatewayurl");
        try {
			message = URLEncoder.encode(message, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String smsGatewayString = formatGatewayString(templateUrl, message, cellNo);
        LOG.debug("Accessing the URL: " + smsGatewayString);
        String gateWayResponse = readFromURL(smsGatewayString);
        LOG.debug("Response: " + gateWayResponse);
        boolean isSuccess = isSuccessful(gateWayResponse);
         if(!isSuccess){
           LOG.error("SMS not successful. Response: " + gateWayResponse);
         }
        return isSuccess;
    }

    public static String readFromURL1(String urlString)
    {
    	 String str ="";
    	try {
            // Create a URL for the desired page
            URL url = new URL(urlString);
            // Read all the text returned by the server
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
           
            while ((str = in.readLine()) != null) {
                // str is one line of text; readLine() strips the newline character(s)
            }
            in.close();
        } catch (MalformedURLException e) {
        	LOG.error(e);
        } catch (IOException e) {
        	LOG.error(e);
        }
        return str;
    }
    public static String readFromURL(String url)
    {
    	String gateWayResponse = new String();
        HttpClient client = new HttpClient();

        //Proxy settings
        boolean isInternetProxyEnabled = false;
        try
        {
        	String isInternetProxyEnabledSTr = PropertiesHandler.getProperty(TBitsPropEnum.KEY_INTERNET_PROXY_ISENABLED);
        	isInternetProxyEnabled = Boolean.parseBoolean(isInternetProxyEnabledSTr);
        	
        }
        catch(Exception ex)
        {
        	LOG.warn("Internet proxy settings not found. Disabling proxy.");
        }
        if(isInternetProxyEnabled)
        {
        	String proxyServer;
        	try
        	{
        		proxyServer = PropertiesHandler.getProperty(TBitsPropEnum.KEY_INTERNET_PROXY_SERVER);
        		int proxyPort = 80;
        		try
        		{
        			proxyPort = Integer.parseInt(PropertiesHandler.getProperty(TBitsPropEnum.KEY_INTERNET_PROXY_PORT));
        		}
        		catch(Exception ex)
        		{
        			LOG.warn("Invalid proxy port defined in property '" + TBitsPropEnum.KEY_INTERNET_PROXY_PORT + "'");
        		}
        		HostConfiguration hc = client.getHostConfiguration();
            	hc.setProxy(proxyServer, proxyPort);
            	client.setHostConfiguration(hc);
            	
            	try
            	{
            	String proxyUser = PropertiesHandler.getProperty(TBitsPropEnum.KEY_INTERNET_PROXY_USER);
            	String proxyPassword = PropertiesHandler.getProperty(TBitsPropEnum.KEY_INTERNET_PROXY_Password);
            	System.out.println("Proxy User: " + proxyUser);
            	System.out.println("Proxy Password " + proxyPassword);
            	//Setting username/password
            	client.getParams().setAuthenticationPreemptive(true);
                client.getState().setProxyCredentials(
                		new AuthScope(proxyServer, proxyPort, null),
                		new UsernamePasswordCredentials(proxyUser, proxyPassword)
                		);
            	}
            	catch(Exception ex)
            	{
            		LOG.warn("Unable to read login/password for proxy");
            	}
        	}
        	catch(Exception ex){
        		LOG.warn("Invalid Proxy settings property '" + TBitsPropEnum.KEY_INTERNET_PROXY_SERVER + "'");
        	}
        	
        }
        
        client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        HttpMethod method = new GetMethod(url);
        
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
        try {
            int respCode = client.executeMethod(method);
          
            if (respCode != HttpStatus.SC_OK) {
                System.err.println("Method failed: " + method.getStatusLine());
              }
            InputStream instream = method.getResponseBodyAsStream();
            byte[] buffer = new byte[100];
            int bytesRead = 0;
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            while((bytesRead =  instream.read(buffer)) >= 0) {
                baos.write(buffer, 0 , bytesRead);
            }
            gateWayResponse = new String(baos.toByteArray() );
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            method.releaseConnection();
        }
        return gateWayResponse;
    }
    public static void main(String[] args) {
    	String out = readFromURL("http://www.google.com");
    	System.out.println(out);
    	//SMSDevice sms = new SMSDevice();
    	//sms.doSms("919849258963", "Testing   message");
//        SMSDevice smsDevice = new SMSDevice();
//        String str = PropertiesHandler.getProperty("transbit.tbits.smsgatewayurl");
        //System.out.println("PropertiesHandler.getProperty(\"transbit.tbits.isenabled\") = " + PropertiesHandler.getProperty("transbit.tbits.isenabled"));
        //System.out.println("smsDevice.formatGatewayString(str, \"Testing\",\"32162187\") = " + smsDevice.formatGatewayString(str, "Testing","32162187"));;
        //smsDevice.isSuccessful("<?xml version='1.0'?><ack refno='1111234' errorcode='0'>OK</ack>");
    }

}
