/**
 * 
 */
package example.addons.first;

import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.IEventHandler;
import transbit.tbits.events.IPostRequestCommitEvent;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class UnManagedPostHandler implements IEventHandler<IPostRequestCommitEvent>{

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getInitialOrder()
	 */
	@Override
	public double getInitialOrder() {
		return -1;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "This is unmanaged handler for post request commit";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#handle(transbit.tbits.events.IEvent)
	 */
	@Override
	public void handle(IPostRequestCommitEvent event)
			throws EventFailureException {
		System.out.println("Executing " + this.getClass().getName());
	}

}
