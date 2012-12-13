package pm;

class Predecessor
{
	public long taskId;
	public int lag = 0;
	public DepType depType = DepType.FS;
	public Predecessor(long taskId, int lag, DepType depType) {
		super();
		this.taskId = taskId;
		this.lag = lag;
		this.depType = depType;
	} 
	
	public Predecessor()
	{
		super();
	}
	
	public String toString()
	{
		return taskId + "" + depType + "+" + lag;
	}
	
}