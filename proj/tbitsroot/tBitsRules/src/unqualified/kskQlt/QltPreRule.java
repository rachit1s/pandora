package kskQlt;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;

import static kskQlt.QltHelper.* ;
import static kskQlt.QltConstants.* ;

public class QltPreRule implements IRule {

	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if(null == ba || null == ba.getSystemPrefix() || !ba.getSystemPrefix().equalsIgnoreCase(QltConstants.QLT_SYSPREFIX))
			return new RuleResult(true,"Ignoring as the ba is not : " + QltConstants.QLT_SYSPREFIX,true);
		
		try 
		{			
			if( isAddRequest )
			{
				QltObject qo = new QltObject( currentRequest );
				validateAddRequest(qo);
				checkRequestUsers(qo) ;
				String inspNo = QltHelper.getInspCallNo(qo,connection);
				Field inspNoField = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), QLT_INSP_NO_FIELD_NAME);
//				RequestEx inRex = extendedFields.get(inspNoField) ;
//				inRex.setVarcharValue(inspNo);		
//				extendedFields.put(inspNoField, inRex);
				
				currentRequest.setObject(inspNoField,inspNo);
				
				disableNotify(currentRequest);
				setState(currentRequest, 1);
				clearNo(QLT_LLOYDS_DOC_NO_FIELD_NAME, currentRequest);
				
				return new RuleResult(true,"QltPreRule for add-request finished successfully",true);
			}
			
			QltObject oldQo = new QltObject(oldRequest);
			int oldState = getState(oldQo);
			
			switch(oldState)
			{
				case 1 : 
					{
						processState1(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 2 : 
					{
						processState2(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 3 : 
					{
						processState3(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 4 : 
					{
						processState4(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 5 : 
					{
						processState5(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 6 : 
					{
						processState6(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 7 : 
					{
						processState7(connection, oldRequest, user,
								currentRequest);
						break; 
					}

					case 8 : 
					{
						processState8(connection, oldRequest, user,
								currentRequest);
						break; 
					}
					
					case 100 :
					{
						processState100(connection, oldRequest, user,
								currentRequest);
						break ;
					}
					
					default :
						throw new TBitsException("Request is trapped in an illegal state.");
				}
			
			return new RuleResult(true, "The QltPreRule finished successfully for update request.", true);
		
		} catch (TBitsException e) 
		{
			e.printStackTrace();
			return new RuleResult(false, e.getDescription(), false);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new RuleResult(false,e.getMessage(),false);
		}		
	}

	private void validateAddRequest(QltObject qo) throws TBitsException 
	{
		if( !qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_IC) ||			
			!qo.decision.getName().equals(QLT_DEC_NONE) ||
			!qo.pdfi.getName().equals(QLT_PDFI_NA) ||
			!( qo.genInspNo == true ) ||
			!( qo.genMdccNo == false )
		  )
		  throw new TBitsException(QltHelper.stateInfo(QLT_FS_PEND_ISS_IC, QLT_DEC_NONE, QLT_PDFI_NA, true, false));  
	}

	public String getName() 
	{
		return "QLT prerule for number generation.";
	}
	
	public double getSequence() 
	{
		return 0;
	}

}
