package transbit.tbits.TVN;

import com.tbitsglobal.tvncore.InterfaceExternalServices;
import com.tbitsglobal.tvncore.TvnServlet;

/**
 * Provides an interface between the subversioning client and the TVN system.
 * 
 * @author Karan Gupta
 *
 */

public class SvnServlet extends TvnServlet {
	
	private static final long serialVersionUID = 1L;

	//====================================================================================

	/**
	 * Returns an object of the AllExternalServices interface implementation.
	 */
	public InterfaceExternalServices getExternalServicesObject(){
		
		return new ExternalServices();
	}

	//====================================================================================

}
