package transbit.tbits.autovue.connector;

import com.cimmetry.vuelink.context.GenericContext;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSGetPropAction;
import com.cimmetry.vuelink.propsaction.DMSProperty;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class GetPropCSI_DocName implements DMSGetPropAction<GenericContext>
{

	@Override
	public DMSProperty execute(GenericContext arg0, DMSSession arg1,
			DMSQuery query, DMSArgument[] arg3, Property arg4)
			throws VuelinkException 
	{
		DocInfo docInfo = new DocInfo( new TbitsDocID(query.getDocID()));
		return new DMSProperty(Property.CSI_DocName,docInfo.getName());
	}
	
}