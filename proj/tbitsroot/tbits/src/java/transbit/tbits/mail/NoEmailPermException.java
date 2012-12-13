package transbit.tbits.mail ;

public class NoEmailPermException extends Exception
{
	public NoEmailPermException(String string) {
		super(string);
	}
	// nothing required in definition.
	
	public String toString()
	{
		return NoEmailPermException.class.getName() + " : " + this.getMessage();
	}
}
