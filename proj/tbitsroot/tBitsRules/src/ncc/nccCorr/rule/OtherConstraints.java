package nccCorr.rule;


import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import nccCorr.others.CorresConstants;

import transbit.tbits.domain.Field;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import static nccCorr.others.CorresConstants.*;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.ProtocolOptionEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;

public class OtherConstraints implements ICorrConstraintPlugin
{
	@Override
	public String getName() {
		return null;
	}

	@Override
	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		/**
		 * 1. 	if( currCo.corrType.getName().equals(CORR_CORR_TYPE_NONE))
			throw new CorrException("You must select one of the valid values in " + cfdn(CORR_CORR_TYPE_FIELD_NAME));
			
			2.if(isAddRequest == false  && oldCo.corrType.getName().equals(CORR_CORR_TYPE_ION) && !currCo.corrType.getName().equals(CORR_CORR_TYPE_ION))
		{
			throw new CorrException("It is not allowed to change the " + cfdn(CORR_CORR_TYPE_FIELD_NAME) + " to other than " + ctdn(CORR_CORR_TYPE_FIELD_NAME,CORR_CORR_TYPE_ION) + " once it is set.");			
		}
		
		 3. // KNPL specific fields check
		if(currCo.logger0User.getFirmCode().equals(CORR_ORIG_KNPL))
		{
			if( currCo.pack == null || currCo.pack.getName().equals(CORR_PACK_NONE) )
				throw new CorrException("You must fill the " + cfdn(CORR_PACKAGE_FIELD_NAME) + " field.");
		}
		
		4. 
		if( currCo.logger0User.getFirmCode().equals(CORR_ORIG_KNPL) || currCo.logger0User.getFirmCode().equals(CORR_ORIG_NCCB) || currCo.logger0User.getFirmCode().equals(CORR_ORIG_NCCP))
		{
			if( currCo.genAtt == null || currCo.genAtt.getName().equals(CORR_GENATT_NONE))
				throw new CorrException("You must fill one of the valid values in " + cfdn(CORR_GENATT_FIELD_NAME));
			
			if( currCo.wbsAtt == null || currCo.wbsAtt.getName().equals(CORR_WBSATT_NONE) )
				throw new CorrException("You must fill one of the valid values in " + cfdn(CORR_WBSATT_FIELD_NAME));				
		}
		
		5. 
		if( currCo.logger0User.getFirmCode().equals(CORR_ORIG_DCPL))
		{			
			if( currCo.discipline == null || currCo.discipline.getName().equals(CORR_DISC_NONE))
				throw new CorrException("You must fill one of the valid values in " + cfdn(CORR_DISCIPLINE_FIELD_NAME));
		}
		
		6. if( currCo.logger0User.getFirmCode().equals(CORR_ORIG_DESEIN))
		{
			if( currCo.location == null )
				throw new CorrException("You must fill on of the valid values in " + cfdn(CORR_LOC_FIELD_NAME));
		}
		 */
		
		// SPECS by vinay
		/*
Nilachal Project 1 x 350 MW

NCC Corres Rules
1.They are two "type" fields called "General Attributes" & "WBS Attributes" and the default value is 'None'. If either of   these values is not selected but   clicked PreviewPDF then it    should throw an exception.
2.A Type Field called "Corr Type" has  a value 'ION'.If it is selected then the Logger,Assignee,Cc,Subscriber should be   limited only to NCC. No other firm should be able to access    this ION.


DCPL Corres Rules
 1.DCPL follows same numbering system for Correspondence as well as DTN.
    Eg:Correspondence - K9210-M-0005
                             DTN - K9210-E-0006


KVK Corres Rules
1.They are three type fields called "General Attributes" , "WBS Attributes" , "Package" nd the default value is 'None'. If either of   these values is not selected but   clicked PreviewPDF     then it should throw an exception.

EDTD Corres Rules

1.They are two "type" fields called "General Attributes" & "WBS Attributes" and the default value is 'None'. If either of   these values is not selected but   clicked PreviewPDF then it        should throw an exception.
*/

		CorrObject coob = (CorrObject) params.get(ICorrConstraintPlugin.CORROBJECT) ;
		Connection con = (Connection) params.get(ICorrConstraintPlugin.CONNECTION);
		
		if( coob.getBa().getSystemPrefix().equals(CorresConstants.CORR_SYSPREFIX))
		{
			noneCorrTypeNotAllowed(con,coob);
			ionCannotBeUpdated(con,coob);
			ionUserRestriction(con,coob);
			packageNoneNotAllowed(con,coob);
			wbsAttNoneNotAllowed(con,coob);
			genAttNoneNotAllowed(con,coob);
			desciplineNoneNotAllowed(con,coob);
			locationNoneNotAllowed(con,coob);
		}
	}

	private void locationNoneNotAllowed(Connection con, CorrObject coob) throws CorrException {
		if( coob.getUserMapUsers().get(0) != null && coob.getUserMapUsers().get(0).getLocation().equals(CORR_ORIG_DESEIN))
		{
			if( coob.getAsString(CORR_LOC_FIELD_NAME) == null )
				throw new CorrException("You must fill on of the valid values in " + Utility.fdn(coob.getBa(),CORR_LOC_FIELD_NAME));
		}
	}

	private void ionUserRestriction(Connection con, CorrObject coob) throws CorrException 
	{
		if( null != coob.getAsString(CORR_CORR_TYPE_FIELD_NAME) && coob.getAsString(CORR_CORR_TYPE_FIELD_NAME).equals(CORR_CORR_TYPE_ION))
		{
			Hashtable<String, ProtocolOptionEntry> opts = coob.getOptionMap();
			ProtocolOptionEntry opt = opts.get(CorresConstants.VALID_ION_AGENCY);
			ArrayList<String> validAgencies = null ;
			if( null != opt ) 
				validAgencies = Utility.splitToArrayList(opt.getValue());
			
			if( null != validAgencies )
			{
				if( !validAgencies.contains(coob.getUserMapUsers().get(0).getLocation()))
					throw new CorrException("You  are not allowed to create ION" );
				
				// now test for assignee, subs and cc
				String [] fieldNames = { Field.ASSIGNEE, Field.SUBSCRIBER, Field.CC } ;
				
				String msg = "" ;
				for( String fieldName : fieldNames )
				{
					String userLogins = coob.getAsString(fieldName);
					if( null != userLogins )
					{
						ArrayList<User> users = Utility.toUsers(userLogins);
						if( null != users )
						{
							String invalidUsers = "" ;
							for( User user : users )
							{
								if( !validAgencies.contains( user.getLocation() ) ) 
								{
									invalidUsers += user.getUserLogin() + ",";
								}
							}
							
							if( !invalidUsers.equals(""))
							{
								msg += "\nFollowing users are not allowed in field " + Utility.fdn(coob.getBa(), fieldName) + " when creating ION\n" + invalidUsers + "\n";
							}
						}
					}
				}
				
				if( !msg.equals(""))
					throw new CorrException(msg);
			}
			else
			{
				throw new CorrException("Nothing configured for : " + VALID_ION_AGENCY + " so not allowed to create ION.");
			}
		}
	}

	private void desciplineNoneNotAllowed(Connection con, CorrObject coob) throws CorrException 
	{
		if( coob.getUserMapUsers().get(0) != null &&  coob.getUserMapUsers().get(0).equals(CORR_ORIG_DCPL))
		{			
			if( coob.getAsString(CORR_DISCIPLINE_FIELD_NAME) == null || coob.getAsString(CORR_DISCIPLINE_FIELD_NAME).equals(CORR_DISC_NONE))
				throw new CorrException("You must fill one of the valid values in " + Utility.fdn(coob.getBa(),CORR_DISCIPLINE_FIELD_NAME));
		}
	}

	private void wbsAttNoneNotAllowed(Connection con, CorrObject coob) throws CorrException 
	{
		if( null != coob.getUserMapUsers().get(0))
		{
			String location = coob.getUserMapUsers().get(0).getLocation() ;
			if( 
				(
					location.equals(CORR_ORIG_KNPL) 
					|| location.equals(CORR_ORIG_EDTD) 
					|| location.equals(CORR_ORIG_NCCP) 
				) 
				&& 
				( 
					coob.getAsString(CORR_WBSATT_FIELD_NAME) == null 
					|| coob.getAsString(CORR_WBSATT_FIELD_NAME).equals(CORR_WBSATT_NONE) 
				)
			)
			{
				throw new CorrException("You must fill one of the valid values in " + Utility.fdn(coob.getBa(),CORR_WBSATT_FIELD_NAME));
			}
		}
	}

	private void genAttNoneNotAllowed(Connection con, CorrObject coob) throws CorrException 
	{
		if( null != coob.getUserMapUsers().get(0))
		{
			String location = coob.getUserMapUsers().get(0).getLocation() ;
			if( 
				(
					location.equals(CORR_ORIG_KNPL) 
					|| location.equals(CORR_ORIG_EDTD) 
					|| location.equals(CORR_ORIG_NCCP) 
				) 
				&& 
				( 
					coob.getAsString(CORR_GENATT_FIELD_NAME) == null 
					|| coob.getAsString(CORR_GENATT_FIELD_NAME).equals(CORR_GENATT_NONE) 
				)
			)
			{
				throw new CorrException("You must fill one of the valid values in " + Utility.fdn(coob.getBa(),CORR_GENATT_FIELD_NAME));
			}
		}
	}

	private void packageNoneNotAllowed(Connection con, CorrObject coob) throws CorrException 
	{
		if( coob.getUserMapUsers().get(0).getLocation().equals(CORR_ORIG_KNPL) 
				&& 
				(
						coob.getAsString(CORR_PACKAGE_FIELD_NAME) == null
						|| coob.getAsString(CORR_PACKAGE_FIELD_NAME).equals(CORR_PACK_NONE)
				)
		)
		{
			throw new CorrException("You must fill the " + Utility.fdn(coob.getBa(),CORR_PACKAGE_FIELD_NAME) + " field.");
		}
	}

	private void ionCannotBeUpdated(Connection con, CorrObject coob) throws CorrException 
	{
		if( null != coob.getPrevRequest() )
		{
			String oldCorrType = coob.getPrevRequest().get(CORR_CORR_TYPE_FIELD_NAME);
			String corrType = coob.getAsString(CORR_CORR_TYPE_FIELD_NAME);

			if( corrType.equals(CORR_CORR_TYPE_ION) && !corrType.equals(CORR_CORR_TYPE_ION))
			{
				throw new CorrException("It is not allowed to change the " + Utility.fdn(coob.getBa(), CORR_CORR_TYPE_FIELD_NAME) + " to other than " + Utility.tdn(coob.getBa(),CORR_CORR_TYPE_FIELD_NAME,CORR_CORR_TYPE_ION) + " once it is set.");
			}
		}
	}

	private void noneCorrTypeNotAllowed(Connection con, CorrObject coob) throws CorrException 
	{
		if( 
				null == coob.getAsString(CORR_CORR_TYPE_FIELD_NAME)
				|| coob.getAsString(CORR_CORR_TYPE_FIELD_NAME).equals(CORR_CORR_TYPE_NONE)
		  )
				throw new CorrException("You must select one of the valid values in " + Utility.fdn(coob.getBa(),CORR_CORR_TYPE_FIELD_NAME));
	}

	@Override
	public double getOrder() {
		return 0;
	}

}
