package transbit.tbits.mail;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

class SendMailUtility
{
	public static void main( String [] args )
	{		
		/**
		 * parameter formats : args[0] = program_name
		 * args[1] = y/n --> if y then it will actually send the mails. 
		 * Else it will simulate sending mails.
		 * rest parameters could are the requests for which the mails are to be sent.
		 * in the format  sys_prefix#request_id	
		 */
		
		boolean sendMail = false ;
		
		//System.out.println("Your arguments is : " + args );
		System.out.println("args[0]=" + args[0] + "args[1]="+ args[1]);
		if(args.length < 2 )
		{
			System.out.println("Usage: program_name <[yYnN]> <sysPrefix#reqeustId> [ <sysPrefix#reqeustId>] ");
			return ;
		}
		
		if( args[0].length() > 1 || (!args[0].equalsIgnoreCase("y") && !args[0].equalsIgnoreCase("n") ) )
		{
			System.out.println("First parameter to it should be y/n. If it is y then the actual mails will be sent. Else it will just simulate the mailing.");
			return ;
		}
		else
		{
			if( args[0].equalsIgnoreCase("y") )
				sendMail = true ;  
		}	
			
		for( int i = 1 ; i < args.length ; i++ ) 
		{
			try
			{
			// expects the format sys_prefix#request_id[#action_id]
				String next = args[i];
				String parts[] = next.split("#");
				
				String sysPrefix = parts[0];
				Integer requestId = Integer.parseInt(parts[1]);
				
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(sysPrefix);
				if( ba == null )
					throw new Exception("index = " + i + " : cannot find ba with sysprefix = " + sysPrefix);
				
				Request request = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);				
				
	        	if (request == null) {		            
	        		throw new Exception("index = " + i + " : cannot find request = " + sysPrefix + "#" + requestId);	        		
		        } else {
		        	if( sendMail == false )
		        	{
		        		System.out.println("DEMO : found the request " + sysPrefix + "#" + requestId);
		        	}
		        	else
		        	{
		        		System.out.println("Actual : trying to send mail to request : " + sysPrefix + "#" + requestId);
		        		TBitsMailer myTBitsMailer = new TBitsMailer(request);	
		        		myTBitsMailer.sendMail();		        		
		        	}
	        	}
			}
			catch(Exception e)
			{
				e.printStackTrace() ;
			}
			System.out.println("SendMailUtility has finished.");
		}
	}
}

