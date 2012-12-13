package transbit.tbits.autovue.connector;

/**
 * Multi-content not considered in this application
 */
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
public class GetPropCSI_IsMultiContent implements
		DMSGetPropAction<ActionContext> {

	// Default constructor
	public GetPropCSI_IsMultiContent() {

	}

	public DMSProperty execute(ActionContext context, DMSSession session,
			DMSQuery query, DMSArgument[] args, Property property)
			throws VuelinkException {

		// Multi-content not considered in this application
		return new DMSProperty(property.getName(), "0");
	}

}
