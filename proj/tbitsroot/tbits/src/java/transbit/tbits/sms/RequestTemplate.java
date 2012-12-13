package transbit.tbits.sms;

import transbit.tbits.domain.Request;

public class RequestTemplate extends Template
{
	Request request;
	public RequestTemplate(Request aRequest)
	{
		this.request = aRequest;
	}
	
	/**
	 * Gets the value corresponding to the a field of request.
	 * If variable is not found sends the variable name surrounded with $
	 */
	protected String getValue(String name)
	{
		String s = request.get(name);
		  if(s == null)
			return "$" + name + "$";
		  else 
			return s;
	}
	
	public static void main(String[] args)
	{
		Request request = new Request();
        request.setDescription("MD");
        request.setSubject("Hi");
        request.setSystemId(1);
        
        RequestTemplate requestTemplate = new RequestTemplate(request);
        String input = "Hi, The description is: $description$ and Subject: $subject$";
        String out;
		try {
			out = requestTemplate.getText(input);
			//System.out.println(out);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}                
	}
}
