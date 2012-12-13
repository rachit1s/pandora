package transbit.tbits.domain;

import java.io.Serializable;

/**
 * 
 * @author nitiraj
 * This is a class that represents the TEXT DataType.
 * As the string and int are associated so they must be properly 
 * encapsulated. 
 * There should be proper heirarchy of DataTypes so that
 * they all can be handled uniformly.
 */
public class TextDataType implements Serializable 
{
	private String text ;
	
	/**
	 * It can have two values as defined in TBitsConstants
	 * 	public static final int CONTENT_TYPE_TEXT = 1;
	public static final int CONTENT_TYPE_HTML = 0;
	 */
	private int contentType ;
	
	public TextDataType(String text, int contentType)
	{
		this.text = text ;
		this.contentType = contentType ;
	}
	
	public String getText()
	{
		return this.text ;
	}
	
	public int getContentType()
	{
		return this.contentType ;
	}
}
