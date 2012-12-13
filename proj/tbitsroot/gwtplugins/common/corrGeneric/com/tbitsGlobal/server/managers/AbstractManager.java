package corrGeneric.com.tbitsGlobal.server.managers;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrManager;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public abstract class AbstractManager implements ICorrManager 
{
	public abstract void refresh() throws CorrException;
}
