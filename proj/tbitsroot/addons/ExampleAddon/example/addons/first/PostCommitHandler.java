/**
 * 
 */
package example.addons.first;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.User;
import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.IEventHandler;
import transbit.tbits.events.IPostRequestCommitEvent;
import transbit.tbits.events.IUpdatePostEvent;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class PostCommitHandler implements IEventHandler<IPostRequestCommitEvent>
{

	/* (non-Javadoc)
	 * @see transbit.tbits.events.EventHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Registers for IPostRequestCommitEvent. This will use the connection object to enter the first name and last name of the user in the addon table";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.EventHandler#handle(transbit.tbits.events.Event)
	 * 
	 * As the commit of the connection takes place after this event. So the changes in it will be persisted in db in case of successful request submission.
	 */
	@Override
	public void handle(IPostRequestCommitEvent event)
			throws EventFailureException 
	{
		Connection con = event.getConnection();
		
		Statement s;
		try 
		{
			s = con.createStatement();
			s.executeUpdate("insert into example_addon_data values ('" + User.lookupByUserId(event.getCurrentRequest().getUserId()).getDisplayName() + "'," + event.getCurrentRequest().getSystemId()+ ","+ event.getCurrentRequest().getRequestId() + "," + event.getCurrentRequest().getMaxActionId() + ")");
			s.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			throw new EventFailureException(e);
		} 
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getInitialOrder()
	 */
	@Override
	public double getInitialOrder() {
		// TODO Auto-generated method stub
		return 5;
	}

}
