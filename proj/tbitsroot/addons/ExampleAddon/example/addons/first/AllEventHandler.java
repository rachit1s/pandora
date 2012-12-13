/**
 * 
 */
package example.addons.first;

import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.IEvent;
import transbit.tbits.events.IEventHandler;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AllEventHandler implements IEventHandler<IEvent>{

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "This will catch all the events and just log them."; 
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#handle(transbit.tbits.events.IEvent)
	 */
	@Override
	public void handle(IEvent event) throws EventFailureException 
	{
		System.out.println("AllEventHandler : Now Executing event : " + event);
		System.out.println("This handler AllEventHandler was registered for all the Events" );
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getInitialOrder()
	 */
	@Override
	public double getInitialOrder() {
		// TODO Auto-generated method stub
		return 3;
	}

}
