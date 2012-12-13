package transbit.tbits.sms;

import java.util.Hashtable;

/**
 * This class is used to Substitute variables in a String Template with original values
 *
 * @author  : Utkarsh Dubey
 *
 * @version : $Id: $
 *
 */

public class Template {

	Hashtable<String, String> myHash = new Hashtable<String, String>();

	public Template() {

	}

	public Template(Hashtable<String, String> myHash) {
		this.myHash = myHash;
	}

	// they will internally use getValue function
public String getText(String templateStr) throws IllegalArgumentException
	{
		String template=templateStr;
		int strlen = template.length();
		int count = 0;
		int dolCount = 0;
		StringBuffer sbi = new StringBuffer();
		StringBuffer sbf = new StringBuffer();
		boolean in = false,flag=false;
		
		char prevCharIs = '~';
		char prevCharWill;
		while (count < strlen - 1) {
			
			if (template.charAt(count) == '$') {
				if (in==false) {
					sbf.append(sbi.toString());
					sbi.setLength(0);
				}
				
				if (prevCharIs != '/') {
					
					dolCount++;
					in=(!in);
				}
				
				
				
				if (in==false) {
					String curValue = getValue(sbi.toString());
					sbf.append(curValue);
					sbi.setLength(0);
					in=false;
				}
				
				prevCharWill=template.charAt(count);
				if (template.charAt(count + 1) == '$')
				{
					throw new IllegalArgumentException("Invalid Template :" + template);
				}
				if(template.charAt(count+1)=='/') {
					prevCharWill=template.charAt(count);
					if(count+2==strlen)flag=true;
					count++;
					continue;
				}
				
				
				sbi.append(template.charAt(count + 1));
				prevCharWill=template.charAt(count+1);
				count += 2;
				
				
			} else
				
			{
				if (template.charAt(count) == '/') {
					sbi.append(template.charAt(count + 1));
					prevCharWill=template.charAt(count+1);
					count += 2;
				} else {
					sbi.append(template.charAt(count));
					prevCharWill=template.charAt(count);
					count++;
				}
			}
			
		
		if(count > strlen-2 && flag ){
			
			if(in==true){
				if(template.charAt(strlen-1) != '$')   
				{	
					sbi.append(template.charAt(strlen-1));
					sbf.append(getValue(sbi.toString()));         	   
				    count++;
				}
				else {
					sbf.append(getValue(sbi.toString()));
				    count++;
				}
			}
			else{
				sbi.append(template.charAt(strlen-1));
				sbf.append(sbi.toString());
			    count++;
			}
		 }
		prevCharIs = prevCharWill;
		}
		
		//sbf.append(sbi.toString());
		if(!flag)sbf.append(sbi.toString());
		return sbf.toString();
	}

	// returns value corresponding to a variable name. if the name doesnt exist,
	// it returns null.
	protected String getValue(String name) {

		String s = myHash.get(name);
		if (s == null)
			return "$" + name + "$";
		else
			return s;
	}

	public static void main(String[] args) {
		try {
			Hashtable<String, String> myHash = new Hashtable<String, String>();
			myHash.put("Anil", "nameid1");
			myHash.put("San$$x", "nameid2");
			myHash.put("San$x123", "nameid3");
			myHash.put("$$Anil$$", "Ambani");
			myHash.put("$Anil$", "1");
			myHash.put("request", "requestID1");
			myHash.put("name", "Sandeep");
			myHash.put("money", "$4.0");
			myHash.put("$5$", "$6$");
			myHash.put("MSG", "hello");
			myHash.put("CELLNO", "9897045592");
			
			Template t = new Template(myHash);
			String x = "$Anil$ == $Anil$ is good.$Anil$";
			System.out.println(t.getText(x));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
