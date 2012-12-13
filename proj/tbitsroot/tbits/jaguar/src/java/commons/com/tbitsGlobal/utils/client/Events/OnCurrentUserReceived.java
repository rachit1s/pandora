package commons.com.tbitsGlobal.utils.client.Events;


/**
 * @author dheeru
 * 
 */
public class OnCurrentUserReceived extends TbitsBaseEvent {
	@Override
	public boolean beforeFire() {
		return true;
	}
}
