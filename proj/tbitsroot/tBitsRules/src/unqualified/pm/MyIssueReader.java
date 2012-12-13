package pm;
import java.util.ArrayList;


public class MyIssueReader implements IIssueReader
{
	public ArrayList<TaskDetails> taskDetails = null;
	public MyIssueReader(ArrayList<TaskDetails> tds)
	{
		taskDetails = tds;
	}
	public TaskDetails getIssueById(long issueId) {
		TaskDetails td = null;
		if(taskDetails == null)
			return td;
		for(TaskDetails t: taskDetails)
		{
			if(t.taskId == issueId)
				return t;
		}
		return td;
	}
}