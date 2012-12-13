package transbit.tbits.autovue.connector;

import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.propsaction.DMSAction;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class ActionOpen implements DMSAction<ActionContext>, DMSDefs{

	@Override
	public Object execute(ActionContext context, DMSSession session, DMSQuery query,
			DMSArgument[] args) throws VuelinkException 
	{
		System.out.println("inside ActionOpen.");
		return new TbitsDocID(query.getOriginalURL()) ;
	}
	
}
