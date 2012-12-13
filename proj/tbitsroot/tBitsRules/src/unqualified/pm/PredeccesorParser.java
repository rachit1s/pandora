package pm;
import java.util.ArrayList;


public class PredeccesorParser {
	/*
	 * Parses the Predecessor expression
	 */
	public static ArrayList<Predecessor> parse(String predecessorStr) {
		ArrayList<Predecessor> preds = new ArrayList<Predecessor>();
		if(predecessorStr == null)
		{
			return preds;
		}
		//break by commas
		String[] predsStr = predecessorStr.split(",");
		for(String predStr:predsStr)
		{
			Predecessor p;
			try{
				p = parseSingle(predStr);
			}
			catch(Exception ex)
			{
				throw new IllegalArgumentException("Invalid Pattern: " + predStr);
			}
			if(p != null)
				preds.add(p);
		}
		return preds;
	}
	
	/*
	 * Parses a single predecessor
	 * Will convert the following grammer:
	 * predStr: taskId
	 * predStr: taskid <FS|SS|SF|FF>
	 * PredStr: taskid <FS|SS|SF|FF> +/- lag
	 * taskId: integer
	 * lag: double
	 */
	public static Predecessor parseSingle(String predStr) throws IllegalArgumentException{
		Predecessor p = new Predecessor();
		int index = 0;
		char ch = '\0';
		//consume spaces/tabs
		while( 
				(index < predStr.length()) 
				&& (
						((ch = predStr.charAt(index++)) == ' ') 
						|| (ch == '\t')
					)
			  );
		if((ch == ' ') || (ch == '\t') || ch == '\0')
		{
			return null;
		}
		
		StringBuilder sb = new StringBuilder(ch + "");
		while( (index < predStr.length()) && Character.isDigit(ch = predStr.charAt(index)))
		{
			sb.append(ch);
			index++;
		}
		long taskId;
		try
		{
			taskId = Long.parseLong(sb.toString());
		}
		catch(Exception ex)
		{
			throw new IllegalArgumentException("Invalid Pattern");
		}
		p.taskId = taskId;
		//consume spaces/tabs
		while( (index < predStr.length()) && (((ch = predStr.charAt(index++)) == ' ') || (ch == '\t')));
		if(index >= predStr.length())
			return p;
		
		//read two letters if these letters are not FS|SS|SF|FF or end of string throw error
		String depTypeStr = ch + "";
		if(index < predStr.length())
			depTypeStr += predStr.charAt(index++);
		DepType depType;
		try
		{
			depType = DepType.valueOf(depTypeStr.toUpperCase());
			p.depType = depType;
		}
		catch(Exception exp)
		{
			throw new IllegalArgumentException("Invalid Pattern");
		}
		//consume spaces/tabs
		while( (index < predStr.length()) && (((ch = predStr.charAt(index++)) == ' ') || (ch == '\t')));
		if(index >= predStr.length())
			return p;
		
		//the next characters should +/- or end of string.
		boolean isPos = true;
		if(ch == '-')
		{
			isPos = false;
		}
		else if(ch != '+')
		{
			throw new IllegalArgumentException("Invalid Pattern");
		}
		//consume spaces/tabs
		while((((ch = predStr.charAt(index++)) == ' ') || (ch == '\t')))
		{
			 if(index >= predStr.length())
			 {
				 throw new IllegalArgumentException("Invalid Pattern");
			 }
		}
			
		//consume double.
		sb = new StringBuilder(ch + "");
		while( 
				(index < predStr.length()) 
				&& (
						Character.isDigit(ch = predStr.charAt(index))
						|| (ch == '.')
					)
			)
		{
			sb.append(ch);
			index++;
		}
		try
		{
			int lag = (int) Math.round(Double.parseDouble(sb.toString()));
			if(!isPos)
				lag = -1 * lag;
			p.lag = lag;
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Invalid Pattern");
		}
		return p;
	}
}
