package transbit.tbits.api;

public class RuleResult {
	private boolean canContinue;
	private boolean isSuccessful;
	private String message;
	public RuleResult(boolean canContinue, String message, boolean isSuccessful)
	{
		this.canContinue = canContinue;
		this.message = message;
		this.isSuccessful = isSuccessful;
	}
	public RuleResult(boolean canContinue, String message)
	{
		this(canContinue, message, true);
	}
	public RuleResult()
	{
		this(true, "", true);
	}
	public RuleResult(boolean isSuccessful)
	{
		this(true, "", isSuccessful);
	}
	public void setCanContinue(boolean canContinue) {
		this.canContinue = canContinue;
	}
	public boolean canContinue() {
		return canContinue;
	}
	public void setSuccessful(boolean isSuccessful) {
		this.isSuccessful = isSuccessful;
	}
	public boolean isSuccessful() {
		return isSuccessful;
	}
	public String getMessage() {
		// TODO Auto-generated method stub
		return message;
	}
	public void setMessage(String message)
	{
		this.message = message;
	}
}
