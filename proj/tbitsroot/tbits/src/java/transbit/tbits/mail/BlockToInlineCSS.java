package transbit.tbits.mail;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transbit.tbits.Helper.TBitsHelper;

/*
 * Converts Block CSS to inline CSS. The main purpose is to 
 * make the formating look better in gmail as gmail only support 
 * the inline CSS not block CSS.
 */
public class BlockToInlineCSS 
{
	
	public BlockToInlineCSS()
	{
		
	}
	public static String ReplaceCSSClassWithInlineStyle(String htmlBlock, CSS css)
	{
		Pattern classPattern = Pattern.compile("class=[\"]?(([ \\t\\n]*[a-z]+[ \\t\\n]*)+)\"");
		int count = 0;
		while(true)
		{
			count++;
			if(count > 10000)
			{
				System.err.println("The replacement iteration is going to loop more than 10000. breaking...");
				break;
			}
			Matcher matcher = classPattern.matcher(htmlBlock);
			if(matcher.find())
			{
				String classAttr = matcher.group(0);
				String classExp = matcher.group(1);
				String newClassPlusStyle = classExprToStyleExp(classExp, css);
//				System.out.println("classAttr: '" + classAttr + "'" );
//				System.out.println("classExp: '" + classExp + "'" );
//				System.out.println("newClassPlusStyle: '" + newClassPlusStyle + "'" );
//				
//				System.out.println("Replacing: \'" + classAttr + "\' with '" + newClassPlusStyle + "'");
				htmlBlock = htmlBlock.replace(classAttr, newClassPlusStyle);
			} 
			else 
				break;
		}
		return htmlBlock;		
	}
	
	public static String ReplaCSSClassWithInlineStyle(String htmlBlock, String cssBlock)
	{
		CSS css = new CSS(cssBlock);
		return ReplaceCSSClassWithInlineStyle(htmlBlock, css);
	}
	
	/*
	 * classExp: sx cw l u b
	 * css: complete css block
	 */
	public static String classExprToStyleExp(String classExp, CSS css)
	{
		StringBuilder style = new StringBuilder();
//		StringBuilder leftClasses = new StringBuilder();
		HashMap styleMap = css.GetStylesMap();
		for(String s: classExp.split(" "))
		{
			String styleName = s.trim();
			if(styleName.length() > 0){
				if(styleMap.containsKey(styleName))
				{
//					System.out.println("Found:" + styleName);
					style.append(styleMap.get(styleName));
				}
				else
				{
					System.out.println("Style Not Found:" + styleName);
					//leftClasses.append(styleName + " ");
				}
			}
		}
		String output = "";
		if(style.length() > 0)
		{
			output += "style=\"" + style.toString() + "\"";
		}
//		if(leftClasses.length() > 0)
//		{
//			output += " class=\"" + leftClasses.toString() + "\"";
//		}
		return output;
	}
	
	public static void main(String [] args) throws IOException
	{
		String cssfileName = "c:\\temp\\html\\tbits.css";
		String sampleHtmlFile = "c:\\temp\\html\\outputwithblockcss.html";
		String myCSS = TBitsHelper.ReadFileToEnd(cssfileName);
		CSS css = new CSS(myCSS);//CSS.loadFromFile(cssfileName);
		String s = ReplaceCSSClassWithInlineStyle(TBitsHelper.ReadFileToEnd(sampleHtmlFile), css);
		System.out.println(s);
	}
}
