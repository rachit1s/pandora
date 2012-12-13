package transbit.tbits.mail;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transbit.tbits.Helper.TBitsHelper;

/*
 * It is simple representation of CSS in the form of key-value pairs.
 * Where key is the name of css-class and value is the its formating rules.
 * It's main utility is converting block CSS to inline CSS. 
 * Right now it only captures the classes. 
 */
public class CSS
{
	HashMap<String, String> stylesMap = null;
	public CSS(String css)
	{
		stylesMap = new HashMap<String, String>();
		
		Pattern p = Pattern.compile("\\.([^:\\.]+)[ \\t\\n]*\\{([^\\}]+)\\}", Pattern.MULTILINE);
		Matcher matcher = p.matcher(css);
		while(matcher.find())
		{
			String name =  matcher.group(1).trim();
			String value = matcher.group(2).replaceAll("\\n", " ").replaceAll("[ \t]+"," ").trim();
			stylesMap.put(name, value);
//			System.out.println("Putting name='" + name + "'" + " value='" + value + "'");
		}
	}
	
	public HashMap GetStylesMap()
	{
		return stylesMap;
	}
	
	public static CSS loadFromFile(String file) throws IOException
	{
		return new CSS(TBitsHelper.ReadFileToEndAsSingleLine(file));		
	}
	
	public static void main(String[] args)
	{
		String f = "c:\\temp\\html\\tbits.css";
		HashMap map;
		try {
			map = loadFromFile(f).GetStylesMap();
			if(map == null)
			{
				System.out.println("No styles found.");
			}
//			else
//				System.out.println(map.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
}

