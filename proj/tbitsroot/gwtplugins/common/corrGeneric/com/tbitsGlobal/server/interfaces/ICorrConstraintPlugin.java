package corrGeneric.com.tbitsGlobal.server.interfaces;

import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public interface ICorrConstraintPlugin 
{
	public static final String CORROBJECT = "CorrObject";
	public static final String CONNECTION = "Connection";
	public void execute(Hashtable<String,Object>params) throws CorrException;
	public String getName();
	public double getOrder();
}
