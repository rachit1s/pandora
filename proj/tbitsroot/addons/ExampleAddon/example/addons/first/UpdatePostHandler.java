/**
 * 
 */
package example.addons.first;

import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.IEventHandler;
import transbit.tbits.events.IUpdatePostEvent;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class UpdatePostHandler implements IEventHandler<IUpdatePostEvent>{

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Registered for IUpdatePostEvent"; 
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#handle(transbit.tbits.events.IEvent)
	 */
	@Override
	public void handle(IUpdatePostEvent event) throws EventFailureException {
		System.out.println("Running " + this.getClass().getName() + " for " + event);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getInitialOrder()
	 */
	@Override
	public double getInitialOrder() {
		// TODO Auto-generated method stub
		return 7;
	}

}
