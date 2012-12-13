package corrGeneric.com.tbitsGlobal.server.interfaces;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public interface ICache<T,S> 
{
	S get(T t) throws CorrException;
}
