package dtn;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import transbit.tbits.api.APIUtil;
import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.mail.IMailPreProcessor;

/**
 * @author nitiraj
 * 
 * It runs for BA with sys_prefix "GMR_DTN" and takes all the files that ends 
 * in ".html" from the Field with name "DTNFile" and appends the contents of these
 * files below the description of the latest action.
 */
public class HtmlInEmail implements IMailPreProcessor 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("dtn");
	private final String DTNFileFieldName = "DTNFile";
	
	@Override
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		try
		{
			ArrayList<String> applicableSysprefix = new ArrayList<String>();
			applicableSysprefix.add("GMR_DTN");
			BusinessArea ba = null;
			ba = BusinessArea.lookupBySystemId(request.getSystemId());
			
			if( ( null == ba ) || ( null != ba && !applicableSysprefix.contains(ba.getSystemPrefix())))
				return ;
			
			Action currentAction = null;
			for(Iterator<Action> iter = actionList.iterator(); iter.hasNext() ;)
			{
				Action action = iter.next();
				if( action.getActionId() == request.getMaxActionId() )
				{
					currentAction = action;
					break;
				}
			}
			if( null == currentAction )
				return ;
				
			currentAction.setDescription(getNewDescription(request, actionFileHash));
		}
		catch(Exception e)
		{
			LOG.error(TBitsLogger.getStackTrace(e));
		}
	}

	/** 
	 * Finds all the html files from DTNFile attachment field and returns its contents.
	 * @param request
	 * @param actionFileHash 
	 * @return
	 * @throws DatabaseException 
	 * @throws IOException 
	 */
	private String getNewDescription(Request request, Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash) throws DatabaseException, IOException 
	{
		Field f = Field.lookupBySystemIdAndFieldName(request.getSystemId(), DTNFileFieldName);
		StringBuffer sb = new StringBuffer();
		
		Collection<ActionFileInfo> afis = actionFileHash.get(request.getMaxActionId());

		if( null == afis )
			return sb.toString() ;
		
		if( null == f )
			return sb.toString();
		
		for( ActionFileInfo afi : afis )
		{
			if( afi.getName().toUpperCase().endsWith(".HTML") )
			{
				File file = new File(APIUtil.getAttachmentLocation() + File.separator + afi.getLocation());
				if( file.exists() )
				{
					
					FileReader fr = new FileReader(file);
					
					BufferedReader br = new BufferedReader(fr);
					
					char [] contents = new char[1024];

					int len = -1 ;
					while( (len = br.read(contents) ) != -1 )
					{
						sb.append(contents, 0, len);
					}
				}
			}
		}
		
		return sb.toString();
	}

	@Override
	public String getMailPreProcessorName() {
		return "Send the HTML report generated in Transmittal inside description of latest action.";
	}

	@Override
	public double getMailPreProcessorOrder() {
		return 0;
	}
}
