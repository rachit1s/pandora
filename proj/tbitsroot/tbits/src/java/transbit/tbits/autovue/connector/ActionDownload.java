package transbit.tbits.autovue.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import transbit.tbits.Helper.Messages;
import com.cimmetry.vuelink.context.GenericContext;
import com.cimmetry.vuelink.defs.DMSDefs;
import com.cimmetry.vuelink.defs.VuelinkException;
import com.cimmetry.vuelink.property.Property;
import com.cimmetry.vuelink.propsaction.DMSAction;
import com.cimmetry.vuelink.propsaction.arguments.DMSArgument;
import com.cimmetry.vuelink.query.DMSQuery;
import com.cimmetry.vuelink.session.DMSSession;
/**
 * 
 * @author Nitiraj Singh Rathore ( nitiraj.r@tbitsglobal.com )
 *
 */
public class ActionDownload implements DMSAction<GenericContext>, DMSDefs{

	@Override
	public Object execute(GenericContext context, DMSSession session, DMSQuery query,
			DMSArgument[] args) throws VuelinkException 
	{
		 /* Sanity checks */
        if (!"download".equalsIgnoreCase(query.getActionName())) {
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Invalid action name within query: " +
                                       query.getActionName());
        }
        if (query.getDocID() == null) {
            throw new VuelinkException(DMS_ERROR_CODE_ERROR,
                                       DMS_ERROR_MSG_NODOCID);
        }
		// Multicontent sends a property
        if (query.getProperties() != null) {
			Property[] props = query.getProperties();
			for ( int i = 0; i < props.length; i++) {
				System.out.println(props[i]);
			}
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Unexpected properties within query: " +
                                       query.getProperties());
        }
        if (args != null && args.length != 0) {
            throw new VuelinkException(DMS_ERROR_CODE_UNKNOWN_ERROR,
                                       "Unexpected arguments to download request");
        }

		DocInfo di = new DocInfo(query.getDocID());
		
		File file = di.getFile();
		
		if( file.exists() == false )
			throw new VuelinkException(DMS_ERROR_CODE_ERROR,Messages.getMessage("NO_ATTACHMENT", ""));
		
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new VuelinkException(DMS_ERROR_CODE_ERROR,Messages.getMessage("NO_ATTACHMENT", ""));
		}
	}
}
