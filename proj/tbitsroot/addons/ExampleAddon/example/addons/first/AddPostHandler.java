/**
 * 
 */
package example.addons.first;

import transbit.tbits.events.EventFailureException;
import transbit.tbits.events.IAddPostEvent;
import transbit.tbits.events.IEventHandler;

/**
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class AddPostHandler implements IEventHandler<IAddPostEvent>{

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getDescription()
	 */
	@Override
	public String getDescription() {
		return "Registered for IAddPostEvent";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#handle(transbit.tbits.events.IEvent)
	 */
	@Override
	public void handle(IAddPostEvent event) throws EventFailureException {
		System.out.println("Running " + this.getClass().getName() + " for " + event);
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.events.IEventHandler#getInitialOrder()
	 */
	@Override
	public double getInitialOrder() {
		// TODO Auto-generated method stub
		return 1;
	}

}
