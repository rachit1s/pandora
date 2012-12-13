package commons.com.tbitsGlobal.utils.client.Events;


public class ToRefreshFieldCache extends TbitsBaseEvent{
	
	public ToRefreshFieldCache() {
		super();
	}
	
	@Override
	public boolean beforeFire() {
		return true;
	}
}
