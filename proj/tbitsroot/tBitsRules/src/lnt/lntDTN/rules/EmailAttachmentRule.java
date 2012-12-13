package lntDTN.rules;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.common.ActionFileInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.Action;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserType;
import transbit.tbits.mail.IMailPreProcessor;

public class EmailAttachmentRule implements IMailPreProcessor {

	public static TBitsLogger LOG = TBitsLogger.getLogger("lntDTN.rules");
	public void executeMailPreProcessor(User user, Request request,
			ArrayList<Action> actionList,
			Hashtable<Integer, Collection<ActionFileInfo>> actionFileHash,
			Hashtable<String, Integer> permissions) 
	{
		
		int maxActionId = request.getMaxActionId();
		Collection<ActionFileInfo> currAtts = actionFileHash.get(maxActionId);
		if( null == currAtts || currAtts.size() == 0 )
		{
			LOG.info("No attachments to handle for request." );
			return ;
		}
		
		String dtnFileName = "DTNFile";
		
		ArrayList<Integer> dtnBas = new ArrayList<Integer>();
		ArrayList<Integer> dtrBas = new ArrayList<Integer>();
		
		String dtnSql = "select distinct(dtn_sys_id) from trn_processes " ;
		String dtrSql = "select distinct(dtr_sys_id) from trn_processes " ;
		
		Connection con = null;
		try
		{
			con = DataSourcePool.getConnection();
			if( null != con )
			{
				PreparedStatement psn = con.prepareStatement(dtnSql);
				ResultSet rsn = psn.executeQuery();
				if( null != rsn )
				{
					while( rsn.next() )
					{
						dtnBas.add(rsn.getInt("dtn_sys_id"));
					}
				}
				rsn.close();
				psn.close();
				
				PreparedStatement psr = con.prepareStatement(dtrSql);
				ResultSet rsr = psr.executeQuery();
				if( null != rsr )
				{
					while(rsr.next())
					{
						dtrBas.add(rsr.getInt("dtr_sys_id"));
					}
				}
				rsr.close();
				psr.close();
			}
			
			if( !dtnBas.contains(request.getSystemId()) && !dtrBas.contains(request.getSystemId()) )
			{
				LOG.info("This email rule is not applicable for this request  of sys_id = " + request.getSystemId());
				return;
			}
			
			Field dtnFileField = Field.lookupBySystemIdAndFieldName(request.getSystemId(), dtnFileName);
			if( null == dtnFileField )
			{
				LOG.info("This rule was applicable but could not find the field with name : " + dtnFileName  + ". So skipping");
				return;
			}
			
			double dtnFilePriority = 0;
			double otherFilePriority = 0; 
			
			switch( user.getUserTypeId() )
			{
				case UserType.EXTERNAL_USER :
				{
					dtnFilePriority = 2;
					otherFilePriority = 1;
					break;
				}
				case UserType.INTERNAL_USER :
				{
					dtnFilePriority = 2;
					otherFilePriority = -1 ; // always link
					break;
				}
				default :
				{
					LOG.info("The user " + user + " was neither internal user nor external user. So skiping the mail preprocesor.");
					return;
				}
			}
			
			for( ActionFileInfo afi : currAtts )
			{
				if( null == afi )
					continue; 
				if( afi.getFieldId() == dtnFileField.getFieldId() )
				{
					afi.setPriority(dtnFilePriority);
				}
				else // for all other files.
				{
					afi.setPriority(otherFilePriority);
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			LOG.error(e);
		}
		finally
		{
			if( null != con )
			{
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		
		
	}

	public String getMailPreProcessorName() {
		return "Sends DTN file as Attachment in Email. Other files as links to internal users but as attachment to external users.";
	}

	public double getMailPreProcessorOrder() 
	{
		return 0;
	}

}
