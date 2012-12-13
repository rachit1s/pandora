package kskQlt;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import com.google.gson.Gson;

import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import static kskQlt.QltConstants.*;
import static kskQlt.QltHelper.gson;
public class QltHelper 
{
	private static final String FINAL_STATE_MSG = "The request has reached its final state. No more updates are allowed on this request.";
	public static final int TYPE_PREFILL = 1 ;
	public static final int TYPE_PRERULE = 2 ;
	public static final Gson gson = new Gson() ;
	public static void disableNotify(Request req)
	{
		req.setNotify(false);
		req.setNotifyLoggers(false);
	}
	
	public static int getState( QltObject qo)
	{
		if( isState1( qo ))return 1 ;
		if( isState2( qo ))return 2 ;
		if( isState3( qo ))return 3 ;
		if( isState4( qo ))return 4 ;
		if( isState5( qo ))return 5 ;
		if( isState6( qo ))return 6 ;
		if( isState7( qo ))return 7 ;
		if( isState8( qo ))return 8 ;
		if( isState100(qo))return 100 ;
		
		return 0 ;
	}
	
	public static void setState( Request req, int state ) throws Exception
	{
		switch(state)
		{
			case 1 : 
				{
					setState1(req);
					break; 
				}

				case 2 : 
				{
					setState2(req);
					break; 
				}

				case 3 : 
				{
					setState3(req);
					break; 
				}

				case 4 : 
				{
					setState4(req);
					break; 
				}

				case 5 : 
				{
					setState5(req);
					break; 
				}

				case 6 : 
				{
					setState6(req);
					break; 
				}

				case 7 : 
				{
					setState7(req);
					break; 
				}

				case 8 : 
				{
					setState8(req);
					break; 
				}
				case 100 :
				{
					setState100(req);
					break;
				}
			}
	}
		
	// QLT_FS_PEND_ISS_IC	QLT_DEC_NONE	QLT_PDFI_NA	 true	FALSE
	public static boolean isState1(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_IC) && 
				qo.decision.getName().equals(QLT_DEC_NONE) &&
				qo.pdfi.getName().equals(QLT_PDFI_NA) &&
				qo.genInspNo == true &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false; 
	}

	//QLT_FS_INSP_CALL_ISS	QLT_DEC_PEND_INSP	QLT_PDFI_NA	FALSE	FALSE
	public static boolean isState2(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_INSP_CALL_ISS) && 
				qo.decision.getName().equals(QLT_DEC_PEND_INSP) &&
				qo.pdfi.getName().equals(QLT_PDFI_NA) &&
				qo.genInspNo == false &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false; 
	}

	//QLT_FS_PEND_ISS_MDCC	QLT_DEC_IRN	QLT_PDFI_YES	FALSE	FALSE
	public static boolean isState3(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_MDCC) && 
				qo.decision.getName().equals(QLT_DEC_IRN) &&
				qo.pdfi.getName().equals(QLT_PDFI_YES) &&
				qo.genInspNo == false &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false;  
	}

	// QLT_FS_PEND_ISS_IC	QLT_DEC_IRN	QLT_PDFI_NO	FALSE	FALSE
	public static boolean isState4(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_IC) && 
				qo.decision.getName().equals(QLT_DEC_IRN) &&
				qo.pdfi.getName().equals(QLT_PDFI_NO) &&
				qo.genInspNo == false &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false;  
	}

	// QLT_FS_PEND_ISS_RE_INSP_CALL	QLT_DEC_NAN	QLT_PDFI_YES/QLT_PDFI_NO	FALSE	FALSE
	public static boolean isState5(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_RE_INSP_CALL) && 
				qo.decision.getName().equals(QLT_DEC_NAN) &&
				(qo.pdfi.getName().equals(QLT_PDFI_YES) || qo.pdfi.getName().equals(QLT_PDFI_NO) ) &&
				qo.genInspNo == false &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false;  
	}

	// QLT_FS_PEND_ISS_MDCC	QLT_DEC_IRN	QLT_PDFI_YES	false	true
	public static boolean isState6(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_MDCC) && 
				qo.decision.getName().equals(QLT_DEC_IRN) &&
				qo.pdfi.getName().equals(QLT_PDFI_YES) &&
				qo.genInspNo == false &&
				qo.genMdccNo == true
		)
			return true ;
		else
			return false;  
	}

	// QLT_FS_PEND_ISS_RE_INSP_CALL	QLT_DEC_NONE	QLT_PDFI_NA	true	FALSE
	public static boolean isState7(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_PEND_ISS_RE_INSP_CALL) && 
				qo.decision.getName().equals(QLT_DEC_NONE) &&
				qo.pdfi.getName().equals(QLT_PDFI_NA) &&
				qo.genInspNo == true &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false;  
	}

	//QLT_FS_REINSP_CALL_ISS	QLT_DEC_PEND_INSP	QLT_PDFI_NA	FALSE	FALSE
	public static boolean isState8(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_REINSP_CALL_ISS) && 
				qo.decision.getName().equals(QLT_DEC_PEND_INSP) &&
				qo.pdfi.getName().equals(QLT_PDFI_NA) &&
				qo.genInspNo == false &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false;  
	}
	
	// QLT_FS_MDCC_ISS	QLT_DEC_IRN	QLT_PDFI_YES	FALSE	FALSE
	public static boolean isState100(QltObject qo)
	{
		if(qo.flowStatus.getName().equals(QLT_FS_MDCC_ISS) && 
				qo.decision.getName().equals(QLT_DEC_IRN) &&
				qo.pdfi.getName().equals(QLT_PDFI_YES) &&
				qo.genInspNo == false &&
				qo.genMdccNo == false
		)
			return true ;
		else
			return false; 
	}

	public static void setDecision(Request req, String value) throws Exception
	{
		Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QLT_DECISION_FIELD_NAME, value);

		if(null != type)
		{
			req.setSeverityId(type);
		}
		else
			throw new TBitsException("Exception while setting the Decision field.");
	}

	public static void setFlowStatus(Request req,String value) throws Exception
	{
		Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QLT_FLOW_STATUS_FIELD_NAME, value);

		if(null != type)
		{
//			req.setStatusId(type);
			req.setObject(Field.STATUS, type);
		}
		else
			throw new TBitsException("Exception while setting the Flow Status field.");
	}
	
	public static void setPDFI(Request req, String value) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(req.getSystemId(), QLT_PDFI_FIELD_NAME);
		Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(req.getSystemId(), QLT_PDFI_FIELD_NAME, value);		
		if(null != type && null != field)
		{
			req.setObject(field, type);
//			RequestEx rex = ef.get(field);
//			rex.setTypeValue(type.getTypeId());			
//			ef.put(field,rex);
		}
		else
			throw new TBitsException("Exception while setting the Pre-Dispatch field.");
	}
	
	public static void setGenInsp(Request req, Boolean value) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(req.getSystemId(), QLT_GEN_INSP_NO_FIELD_NAME);
		if( null != field )
		{
//			RequestEx rex = ef.get(field);
//			rex.setBitValue(value);			
//			ef.put(field,rex);
			req.setObject(field, value);
		}
		else
		{
			throw new TBitsException("Exception while setting the generate Inspection call field.");
		}
	}
	
	public static void setGenMdcc(Request req, Boolean value) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(req.getSystemId(), QLT_GEN_MDCC_NO_FIELD_NAME);
		if( null != field )
		{
			req.setObject(field, value);
//			RequestEx rex = ef.get(field) ;
//			rex.setBitValue(value);			
//			ef.put(field,rex);
		}
		else
		{
			throw new TBitsException("Exception while setting the generate MDCC field.");
		}
	}
	
	public static void setMdccNo(Request req, String value) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(req.getSystemId(), QLT_MDCC_NO_FIELD_NAME);
		if( null != field )
		{
			req.setObject(field, value);
//			RequestEx rex = ef.get(field) ;
//			rex.setVarcharValue(value);			
//			ef.put(field,rex);
		}
		else
		{
			throw new TBitsException("Exception while setting the MDCC No. field.");
		}
	}
	
	public static void setLloydsNo(Request req, String value) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(req.getSystemId(), QLT_LLOYDS_DOC_NO_FIELD_NAME);
		if( null != field )
		{
			req.setObject(field,value);
//			RequestEx rex = ef.get(field) ;
//			rex.setVarcharValue(value);			
//			ef.put(field,rex);
		}
		else
		{
			throw new TBitsException("Exception while setting the Lloyds no. field.");
		}
	}
	
	public static String getTypeList(int sysId, String fieldName, String types ) throws Exception
	{
		String list = "";
		if( null == types || types.trim().equals(""))
			return list ;
		
		String[] typeList = types.split(",");
		for ( String typeName : typeList )
		{
			Type type = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, fieldName, typeName);
			list += type.getDisplayName() + "/";
		}
		if( !list.equals("") )
			list = list.substring(0,list.length()-1);
		
		return list ;
	}
	public static String stateInfo(String fs, String dec, String pdfi, boolean gin, boolean gmn )
	{
		try
		{
			String msg = "Expected field values are:<br>";
			BusinessArea ba = BusinessArea.lookupBySystemPrefix(QLT_SYSPREFIX);
			
			Field field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_FLOW_STATUS_FIELD_NAME);
			String typeList = getTypeList(ba.getSystemId(), QLT_FLOW_STATUS_FIELD_NAME, fs);
			msg += field.getDisplayName() + ":" + typeList + "<br>";
			
			field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_DECISION_FIELD_NAME);
			typeList = getTypeList(ba.getSystemId(), QLT_DECISION_FIELD_NAME, dec);
			msg += field.getDisplayName() + ":" + typeList + "<br>";
	
			field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_PDFI_FIELD_NAME);
			typeList = getTypeList(ba.getSystemId(), QLT_PDFI_FIELD_NAME, pdfi);
			msg += field.getDisplayName() + ":" + typeList + "<br>";
	
			field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_GEN_INSP_NO_FIELD_NAME);
			msg += field.getDisplayName() + ":" + gin + "<br>";
			
			field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_GEN_MDCC_NO_FIELD_NAME);
			msg += field.getDisplayName() + ":" + gmn + "<br>";
			
			return msg;
		}
		catch(Exception e)
		{
			LOG.info(TBitsLogger.getStackTrace(e));
			return "";
		}		
	}
	
	public static void setInspNo(Request req, String value) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(req.getSystemId(), QLT_INSP_NO_FIELD_NAME);
		if( null != field )
		{
			req.setObject(field, value);
//			RequestEx rex = ef.get(field) ;
//			rex.setVarcharValue(value);
//			ef.put(field,rex);
		}
		else
		{
			throw new TBitsException("Exception while setting the Inspection call no. field.");
		}
	}

	//1 QLT_FS_PEND_ISS_IC	QLT_DEC_NONE	QLT_PDFI_NA	true	FALSE
	public static void setState1(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_PEND_ISS_IC);
		setDecision(req, QLT_DEC_NONE);
		setPDFI(req, QLT_PDFI_NA);
		setGenInsp(req, true);
		setGenMdcc(req, false);
	}
	
	//2	QLT_FS_INSP_CALL_ISS	QLT_DEC_PEND_INSP	QLT_PDFI_NA	FALSE	FALSE
	public static void setState2(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_INSP_CALL_ISS);
		setDecision(req, QLT_DEC_PEND_INSP);
		setPDFI(req, QLT_PDFI_NA);
		setGenInsp(req, false);
		setGenMdcc(req, false);
	}

	//3	QLT_FS_PEND_ISS_MDCC	QLT_DEC_IRN	QLT_PDFI_YES	FALSE	FALSE
	public static void setState3(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_PEND_ISS_MDCC);
		setDecision(req, QLT_DEC_IRN);
		setPDFI(req, QLT_PDFI_YES);
		setGenInsp(req, false);
		setGenMdcc(req,false);
	}

// 4	QLT_FS_PEND_ISS_IC	QLT_DEC_IRN	QLT_PDFI_NO	FALSE	FALSE
	public static void setState4(Request req)throws Exception
	{
		setFlowStatus(req, QLT_FS_PEND_ISS_IC);
		setDecision(req,QLT_DEC_IRN);
		setPDFI(req, QLT_PDFI_NO);
		setGenInsp(req,false);
		setGenMdcc(req,false);
	}

	// 5	QLT_FS_PEND_ISS_RE_INSP_CALL	QLT_DEC_NAN	QLT_PDFI_YES/QLT_PDFI_NO	FALSE	FALSE
	public static void setState5(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_PEND_ISS_RE_INSP_CALL);
		setDecision(req, QLT_DEC_NAN);
//		setPDFI(req, ef, QLT_PDFI_NA); // do not change the PDFI value
		setGenInsp(req, false);
		setGenMdcc(req, false);
	}

// 6	QLT_FS_PEND_ISS_MDCC	QLT_DEC_IRN	QLT_PDFI_YES	false	true
	public static void setState6(Request req) throws Exception
	{
		setFlowStatus(req,QLT_FS_PEND_ISS_MDCC);
		setDecision(req,QLT_DEC_IRN);
		setPDFI(req,QLT_PDFI_YES);
		setGenInsp(req, false);
		setGenMdcc(req, true);
	}

// 7	QLT_FS_PEND_ISS_RE_INSP_CALL	QLT_DEC_NONE	QLT_PDFI_NA	true	FALSE
	public static void setState7(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_PEND_ISS_RE_INSP_CALL);
		setDecision(req, QLT_DEC_NONE);
		setPDFI(req, QLT_PDFI_NA);
		setGenInsp(req, true);
		setGenMdcc(req, false);
	}

	// 8	QLT_FS_REINSP_CALL_ISS	QLT_DEC_PEND_INSP	QLT_PDFI_NA	FALSE	FALSE
	public static void setState8(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_REINSP_CALL_ISS);
		setDecision(req, QLT_DEC_PEND_INSP);
		setPDFI(req, QLT_PDFI_NA);
		setGenInsp(req, false);
		setGenMdcc(req,false);
	}
	
	// 100	QLT_FS_MDCC_ISS	QLT_DEC_IRN	QLT_PDFI_YES	FALSE	FALSE
	public static void setState100(Request req) throws Exception
	{
		setFlowStatus(req, QLT_FS_MDCC_ISS);
		setDecision(req, QLT_DEC_IRN);
		setPDFI(req, QLT_PDFI_YES);
		setGenInsp(req, false);
		setGenMdcc(req, false);
	}	

	private static boolean hasNewAttachment(String fieldName,
			Request oldRequest, Request currentRequest) 
	{
		String currAttachList = currentRequest.get(fieldName);
		String oldAttachList = oldRequest.get(fieldName);
		
		ArrayList<String> attList = getAttachListForRequest(currAttachList,oldAttachList);
		if( attList.size() > 0 )
			return true ;
		else			
			return false;
	}

	public static ArrayList<String> getAttachListForRequest( String currAttachList , String prevAttachList ) 
	{	
		ArrayList<String> attList = new ArrayList<String>() ;
		if( null == currAttachList || currAttachList.trim().equals("") )
			return attList ;
		
		Collection<AttachmentInfo> currAttach = AttachmentInfo.fromJson( currAttachList ) ;
		
		if( null != prevAttachList )
		{			
			Collection<AttachmentInfo> prevAttach = AttachmentInfo.fromJson( prevAttachList ) ;
			
			for( Iterator<AttachmentInfo> curr = currAttach.iterator() ; curr.hasNext() ;  )
			{
				AttachmentInfo c = curr.next() ;
				boolean incl = true ;
				for( Iterator<AttachmentInfo> prev = prevAttach.iterator() ; prev.hasNext() ; )
				{
					AttachmentInfo p = prev.next() ;
					if( p.requestFileId == c.requestFileId && p.repoFileId == c.repoFileId )
					{
						incl = false ;
						break ;
					}
				}
				
				if(incl)
				{
					attList.add(c.name) ;
					System.out.println("Including file : " + c.name );
				}
				else
					System.out.println("Excluding file : " +c.name);
				
			}
		}
		else
		{
			for( Iterator<AttachmentInfo> curr = currAttach.iterator() ; curr.hasNext() ;  )
			{
				attList.add( curr.next().name ) ;
			}
		}
		
		
		return attList ;
	}

	// QLT_FS_PEND_ISS_IC	QLT_DEC_NONE	QLT_PDFI_NA	?=false	FALSE
	public static void processState1( Connection connection, Request oldRequest,User user,
			Request currentRequest ) throws Exception
	{ 	
		QltObject nqo = new QltObject(currentRequest);
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_IC) &&
				nqo.decision.getName().equals(QLT_DEC_NONE) &&
				nqo.pdfi.getName().equals(QLT_PDFI_NA) &&
				nqo.genInspNo == false &&
				nqo.genMdccNo == false
		)
		{
			checkRequestUsers(nqo) ;
			
			if( !hasNewAttachment( QLT_INSP_FILE_FIELD_NAME, oldRequest, currentRequest ) )
				throw new TBitsException("You must upload a new document in the " + 
						Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), QLT_INSP_FILE_FIELD_NAME).getDisplayName() + " Attachment") ;		
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_PEND_ISS_IC, QLT_DEC_NONE, QLT_PDFI_NA, false, false));
		
		setState2(currentRequest);
	}
	
	public static void checkRequestUsers(QltObject nqo) throws TBitsException 
	{		
		StringBuffer msg = new StringBuffer() ;
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_IC))
		{			
			String sub = mustHave(nqo.subscribers,"SEPCOQuality");
			if( !sub.equals(""))
				msg.append("Subscribers must have : " + sub );
		}
		if( nqo.flowStatus.getName().equals(QLT_FS_INSP_CALL_ISS))
		{
			String sub = mustHave(nqo.subscribers,"KMPCLQuality,SEPCOQuality") ;
			String ass = mustHave(nqo.assignees,"LRA") ;
			if( !sub.equals(""))
				msg.append("Subscribers must have : " + sub );
			if( !ass.equals(""))
				msg.append("<br />Assignees must have : " + ass ) ;
		}
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_MDCC))
		{
			String sub = mustHave(nqo.subscribers,"SEPCOQuality,LRA");
			String ass = mustHave(nqo.assignees,"KMPCLQuality") ;
			if( !sub.equals(""))
				msg.append("Subscribers must have : " + sub );
			if( !ass.equals(""))
				msg.append("<br />Assignees must have : " + ass ) ;
		}
		if( nqo.flowStatus.getName().equals(QLT_FS_MDCC_ISS))
		{
			String sub = mustHave(nqo.subscribers,"SEPCO,KMPCLQuality");
//			String ass = mustHave(nqo.assignees,"KMPCLQuality") ;
			if( !sub.equals(""))
				msg.append("Subscribers must have : " + sub );
//			if( !ass.equals(""))
//				msg.append("\nAssignees must have : " + ass ) ;
		}
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_RE_INSP_CALL))
		{
			String sub = mustHave(nqo.subscribers,"LRA,KMPCLQuality");
			String ass = mustHave(nqo.assignees,"SEPCOQuality") ;
			if( !sub.equals(""))
				msg.append("Subscribers must have : " + sub );
			if( !ass.equals(""))
				msg.append("<br/>Assignees must have : " + ass ) ;
		}
		
		if(nqo.flowStatus.getName().equals(QLT_FS_REINSP_CALL_ISS))
		{
			String sub = mustHave(nqo.subscribers,"KMPCLQuality,SEPCOQuality");
			String ass = mustHave(nqo.assignees,"LRA") ;
			if( !sub.equals(""))
				msg.append("Subscribers must have : " + sub );
			if( !ass.equals(""))
				msg.append("<br/>Assignees must have : " + ass ) ;
		}
		
		if( !msg.toString().trim().equals("") )
		{	
			msg.append("<br/>These requirements will be included for your convenience.");
			throw new TBitsException(msg.toString());		
		}
	}

	private static String mustHave(ArrayList<User> rus, String ul) 
	{
		String [] logins = ul.split(",");
		String shouldContain = "" ;
		for(String l : logins )
		{
			boolean contains = false ;
			for( User user : rus )
			{
				if( user.getUserLogin().equals(l) )
					contains = true ;
			}
			if( contains == false )
				shouldContain += l ;
		}
		
		return shouldContain ;
	}

	// QLT_FS_INSP_CALL_ISS	?	?	FALSE	FALSE
	public static void processState2(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		int nextState = 0;
		QltObject nqo = new QltObject(currentRequest);
		if( nqo.genInspNo == false && nqo.genMdccNo == false && nqo.flowStatus.getName().equals(QLT_FS_INSP_CALL_ISS) )
		{
			checkRequestUsers(nqo) ;
			
			if( nqo.decision.getName().equals(QLT_DEC_NAN) && ( nqo.pdfi.getName().equals(QLT_PDFI_YES) || nqo.pdfi.getName().equals(QLT_PDFI_NO) ))
			{
				nextState = 5 ;
				clearNo(QLT_INSP_NO_FIELD_NAME, currentRequest);
			}
			else if( nqo.decision.getName().equals(QLT_DEC_IRN) && nqo.pdfi.getName().equals(QLT_PDFI_YES) )
			{
				nextState = 3 ;			
			}			
			else if( nqo.decision.getName().equals(QLT_DEC_IRN) && nqo.pdfi.getName().equals(QLT_PDFI_NO) )
			{
				nextState = 4 ;
				clearNo(QLT_INSP_NO_FIELD_NAME, currentRequest);
			}
			else
				throw new TBitsException(stateInfo(QLT_FS_INSP_CALL_ISS, QLT_DEC_NAN+","+QLT_DEC_IRN, QLT_PDFI_YES+","+QLT_PDFI_NO, false, false));			
			
			if( !hasNewAttachment( QLT_LLOYDS_FILE_FIELD_NAME, oldRequest, currentRequest ) )
				throw new TBitsException("You must upload a file in " + Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), QLT_LLOYDS_FILE_FIELD_NAME).getDisplayName() + " Attachment field.") ;
			
			validateAndSetLloydsNo( connection,currentRequest );
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_INSP_CALL_ISS, QLT_DEC_NAN+","+QLT_DEC_IRN, QLT_PDFI_NA+","+QLT_PDFI_YES+","+QLT_PDFI_NO, false, false));
		
		setState(currentRequest, nextState);		
	}

	public static void clearNo(String fieldName, Request currentRequest) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
	
		currentRequest.setObject(field, "");
//		RequestEx rex = ef.get(field);
//		rex.setVarcharValue("");
//		ef.put(field, rex);
	}

	//QLT_FS_PEND_ISS_MDCC	QLT_DEC_IRN	QLT_PDFI_YES	FALSE	?=true
	public static void processState3(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		QltObject nqo = new QltObject(currentRequest);
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_MDCC) && 
				nqo.decision.getName().equals(QLT_DEC_IRN) &&
				nqo.pdfi.getName().equals(QLT_PDFI_YES) &&
				nqo.genInspNo == false &&
				nqo.genMdccNo == true
		)
		{
			checkRequestUsers(nqo) ;
			// yes this state is correct
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_PEND_ISS_MDCC,QLT_DEC_IRN, QLT_PDFI_YES, false, true));
		
		// generating mdcc no. 
		String nextMdccNo = getMdccNo(nqo, connection);
		setMdccNo(currentRequest, nextMdccNo);
		disableNotify(currentRequest);
		setState6(currentRequest);
	}


	// QLT_FS_PEND_ISS_IC	QLT_DEC_IRN	QLT_PDFI_NO	?=true	FALSE
	public static void processState4( Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		QltObject nqo = new QltObject(currentRequest);
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_IC) && 
				nqo.decision.getName().equals(QLT_DEC_IRN) &&
				nqo.pdfi.getName().equals(QLT_PDFI_NO) &&
				nqo.genInspNo == true &&
				nqo.genMdccNo == false
		)
		{
			checkRequestUsers(nqo) ;
			// yes this state is correct
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_PEND_ISS_IC,QLT_DEC_IRN, QLT_PDFI_NO, true, false));
		
		String nextInspNo = getInspCallNo(nqo, connection);
		setInspNo(currentRequest, nextInspNo);
		disableNotify(currentRequest);
		clearNo(QLT_LLOYDS_DOC_NO_FIELD_NAME, currentRequest);
		setState1(currentRequest);
	}

//		QLT_FS_PEND_ISS_RE_INSP_CALL	QLT_DEC_NAN	QLT_PDFI_NA	?=true	FALSE
	public static void processState5(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		QltObject nqo = new QltObject(currentRequest);
		QltObject oqo = new QltObject(oldRequest);
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_RE_INSP_CALL) && 
				nqo.decision.getName().equals(QLT_DEC_NAN) &&
				(nqo.pdfi.getName().equals(oqo.pdfi.getName())) &&
				nqo.genInspNo == true &&
				nqo.genMdccNo == false
		)
		{
			checkRequestUsers(nqo) ;
			// yes this state is correct
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_PEND_ISS_RE_INSP_CALL,QLT_DEC_NAN, oqo.pdfi.getName(), true, false));
		
		String nextInspNo = getInspCallNo(nqo, connection);
		setInspNo(currentRequest, nextInspNo);
		disableNotify(currentRequest);
		clearNo(QLT_LLOYDS_DOC_NO_FIELD_NAME, currentRequest);
		setState7(currentRequest);
	}

	//QLT_FS_PEND_ISS_MDCC	QLT_DEC_IRN	QLT_PDFI_YES	false	?=false
	public static void processState6(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		QltObject nqo = new QltObject(currentRequest);
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_MDCC) && 
				nqo.decision.getName().equals(QLT_DEC_IRN) &&
				nqo.pdfi.getName().equals(QLT_PDFI_YES) &&
				nqo.genInspNo == false &&
				nqo.genMdccNo == false
		)
		{
			// yes this state is correct 
			checkRequestUsers(nqo) ;
			// check if the file has been uploaded or not
			if( !hasNewAttachment(QLT_MDCC_FILE_FIELD_NAME, oldRequest, currentRequest))
				throw new TBitsException("You must upload a file in " + Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), QLT_MDCC_FILE_FIELD_NAME).getDisplayName() + " Attachment field.") ;
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_PEND_ISS_MDCC,QLT_DEC_IRN, QLT_PDFI_YES, false, false));
		
		setState100(currentRequest);
	}

	// QLT_FS_PEND_ISS_RE_INSP_CALL	QLT_DEC_NONE	QLT_PDFI_NA	?=false	FALSE
	public static void processState7(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		QltObject nqo = new QltObject(currentRequest);
		
		if( nqo.flowStatus.getName().equals(QLT_FS_PEND_ISS_RE_INSP_CALL) && 
				nqo.decision.getName().equals(QLT_DEC_NONE) &&
				nqo.pdfi.getName().equals(QLT_PDFI_NA) &&
				nqo.genInspNo == false &&
				nqo.genMdccNo == false
		)
		{
			// yes this state is correct // 
			checkRequestUsers(nqo) ;
			
			//check if the file has been uploaded or not
			if( !hasNewAttachment(QLT_INSP_FILE_FIELD_NAME, oldRequest, currentRequest))
				throw new TBitsException("You must upload a file in " + Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), QLT_INSP_FILE_FIELD_NAME).getDisplayName() + " Attachment field.") ;
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_PEND_ISS_RE_INSP_CALL,QLT_DEC_NONE, QLT_PDFI_NA, false, false));
		
		setState8(currentRequest);
	}

//	QLT_FS_REINSP_CALL_ISS	?	?	FALSE	FALSE
	public static void processState8(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		int nextState = 0;
		QltObject nqo = new QltObject(currentRequest);
		if( nqo.genInspNo == false && nqo.genMdccNo == false && nqo.flowStatus.getName().equals(QLT_FS_REINSP_CALL_ISS) )
		{
			checkRequestUsers(nqo) ;
			
			if( nqo.decision.getName().equals(QLT_DEC_NAN) && (nqo.pdfi.getName().equals(QLT_PDFI_YES) || nqo.pdfi.getName().equals(QLT_PDFI_NO) ))
			{
				nextState = 5 ;
				clearNo(QLT_INSP_NO_FIELD_NAME, currentRequest);
			}
			else if( nqo.decision.getName().equals(QLT_DEC_IRN) && nqo.pdfi.getName().equals(QLT_PDFI_YES) )
			{
				nextState = 3 ;
			}
			else if( nqo.decision.getName().equals(QLT_DEC_IRN) && nqo.pdfi.getName().equals(QLT_PDFI_NO) )
			{
				nextState = 4 ;
				clearNo(QLT_INSP_NO_FIELD_NAME, currentRequest);
			}
			else
				throw new TBitsException(stateInfo(QLT_FS_REINSP_CALL_ISS, QLT_DEC_NAN+","+QLT_DEC_IRN, QLT_PDFI_YES+","+QLT_PDFI_NO, false, false));
			
			if( !hasNewAttachment( QLT_LLOYDS_FILE_FIELD_NAME, oldRequest, currentRequest ) )
				throw new TBitsException("You must upload a file in " + Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), QLT_LLOYDS_FILE_FIELD_NAME).getDisplayName() + " Attachment field.") ;
			
			validateAndSetLloydsNo(connection,currentRequest);
		}
		else
			throw new TBitsException(stateInfo(QLT_FS_INSP_CALL_ISS, QLT_DEC_NAN+","+QLT_DEC_IRN, QLT_PDFI_NA+","+QLT_PDFI_YES+","+QLT_PDFI_NO, false, false));
		
		setState(currentRequest, nextState);
	}
	
	private static void validateAndSetLloydsNo(Connection connection, Request cr) throws Exception
	{
		String lloydsNo = cr.get(QLT_LLOYDS_DOC_NO_FIELD_NAME);
		String sanitizedLloydsNo = sanitize(lloydsNo);
		if( sanitizedLloydsNo == null || sanitizedLloydsNo.equals("") )
			throw new TBitsException("You must fill the " + Field.lookupBySystemIdAndFieldName(cr.getSystemId(), QLT_LLOYDS_DOC_NO_FIELD_NAME).getDisplayName() + " field.") ;
		
		checkUniqueness(connection,sanitizedLloydsNo, cr);

		setLloydsNo(cr, sanitizedLloydsNo);
	}

	private static void checkUniqueness(Connection con, String lloydsNo, Request cr) throws Exception
	{
		Field field = Field.lookupBySystemIdAndFieldName(cr.getSystemId(), QLT_LLOYDS_DOC_NO_FIELD_NAME);
		String query = "select * from actions_ex where " + Field.BUSINESS_AREA + "=" + cr.getSystemId() 
						+ " and field_id=" + field.getFieldId() + " and varchar_value='" + lloydsNo +"'";
		
		PreparedStatement ps = null ;
		ResultSet rs = null ;
		try
		{
			ps = con.prepareStatement(query);
			rs = ps.executeQuery();
			
			if( rs != null && rs.next() == true )
			{
				throw new TBitsException("The value ( " + lloydsNo + " ) mentioned in " + field.getDisplayName() + " is not unique.<br>" +
						"It already exists in request : " + QLT_SYSPREFIX + "#" + rs.getInt(Field.REQUEST) + "#" + rs.getInt(Field.ACTION) ) ;
			}
		}
		finally
		{
			if(null != ps)
				ps.close() ;
			if( null != rs )
				rs.close() ;
		}
			
	}

	private static String sanitize(String lloydsNo) {
		// TODO : This is not metioned rt now so returning the same string after trimming
		if( null == lloydsNo )
			return null;
		else
			return lloydsNo.trim();
	}

	public static void processState100(Connection connection, Request oldRequest,User user,
			Request currentRequest) throws Exception
	{
		throw new TBitsException(FINAL_STATE_MSG);
	}

	public static String getMdccPrefix(QltObject qo) 
	{
		String pf = QltConstants.QLT_MDCC_CALL_NO_PREFIX ;
		pf += "-" + qo.subAreaCode.getName() ;
		pf += "-" + qo.unitNumber.getDescription();
		return pf;
	}

	public static String getMdccNo(QltObject qo, Connection con) throws TBitsException {
		String prefix = getMdccPrefix(qo);
		int maxNo = QltConstants.incrAndGetMaxId( prefix, con ) ;
		DecimalFormat df = new DecimalFormat("0000");
		String maxNoStr = df.format(maxNo);
		
		return prefix + maxNoStr;
	}

	public static String getInspCallNo(QltObject qo,Connection con) throws TBitsException 
	{
		String prefix = QltConstants.QLT_INSP_CALL_NO_PREFIX ;
		int maxNo = QltConstants.incrAndGetMaxId( prefix, con ) ;
		DecimalFormat df = new DecimalFormat("000");
		String maxNoStr = df.format(maxNo);
		
		return prefix + "-" + maxNoStr;
	}
	
	public static Hashtable<String,String>  prefillState100(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		params.put("message_value",FINAL_STATE_MSG);
		
		return params;		
	}

	public static Hashtable<String,String>  prefillState8(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		ArrayList<String>pdfi = new ArrayList<String>() ;
		pdfi.add(QLT_PDFI_YES);
		pdfi.add(QLT_PDFI_NO);
		
		ArrayList<String> dec = new ArrayList<String>();
		dec.add(QLT_DEC_IRN);
		dec.add(QLT_DEC_NAN);
		
		ArrayList<String> fs = new ArrayList<String>();
		fs.add(qo.flowStatus.getName());
		
		String msg = "Please make a decision and select value in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_PDFI_FIELD_NAME).getDisplayName() 
		+ " field.<br>Upload a file in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_LLOYDS_FILE_FIELD_NAME).getDisplayName() + 
		"<br>Fill a unique no. in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_LLOYDS_DOC_NO_FIELD_NAME).getDisplayName();
		
		setPrefillTags(params, fs, dec, pdfi, false, false, msg);
		return params;		
	}

	public static Hashtable<String,String>  prefillState7(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		fillStateTypes(qo,params);
		params.put("genInsp_checked", "false");
		params.put("message_value", "Please upload a file in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_INSP_FILE_FIELD_NAME).getDisplayName() + " Attachment and submit.");
		return params;		
	}

	public static Hashtable<String,String>  prefillState6(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		fillStateTypes(qo, params);
		params.put("genMdcc_checked","false");
		params.put("message_value", "Please upload a file in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_MDCC_FILE_FIELD_NAME).getDisplayName() + " Attachment and submit.");
		return params;
	}

	public static Hashtable<String,String>  prefillState4(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		fillStateTypes(qo, params);
		String msg = "Check that " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_GEN_INSP_NO_FIELD_NAME).getDisplayName() + " field is selected and submit." ;
		params.put("message_value", msg);
		params.put("genInsp_checked","true");
		return params;		
	}

	public static Hashtable<String,String>  prefillState3(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		fillStateTypes(qo, params);
		String msg = "Check that " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_GEN_MDCC_NO_FIELD_NAME).getDisplayName() + " field is selected and submit." ;
		params.put("message_value", msg);
		params.put("genMdcc_checked","true");
		return params;		
	}

	public static Hashtable<String,String>  prefillState5(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		fillStateTypes(qo, params);
		String msg = "Check that " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_GEN_INSP_NO_FIELD_NAME).getDisplayName() + " field is selected and submit." ;
		params.put("message_value", msg);
		params.put("genInsp_checked","true");
		return params;		
	}

	public static Hashtable<String,String>  prefillState2(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		ArrayList<String>pdfi = new ArrayList<String>() ;
		pdfi.add(QLT_PDFI_YES);
		pdfi.add(QLT_PDFI_NO);
		
		ArrayList<String> dec = new ArrayList<String>();
		dec.add(QLT_DEC_IRN);
		dec.add(QLT_DEC_NAN);
		
		ArrayList<String> fs = new ArrayList<String>();
		fs.add(qo.flowStatus.getName());
		
		String msg = "Please make a decision and select value in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_PDFI_FIELD_NAME).getDisplayName() 
		+ " field.<br>Upload a file in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_LLOYDS_FILE_FIELD_NAME).getDisplayName() + 
		"<br>Fill a unique no. in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_LLOYDS_DOC_NO_FIELD_NAME).getDisplayName();
		
		setPrefillTags(params, fs, dec, pdfi, false, false, msg);
		
		return params;		
	}

	public static Hashtable<String,String>  prefillState1(QltObject qo, Hashtable<String,String> params,BusinessArea ba) throws Exception 
	{
		fillStateTypes(qo, params);
		params.put("genInsp_checked", "false");
		params.put("genMdcc_checked", "false");
		params.put("message_value", "Please upload a file in " + Field.lookupBySystemIdAndFieldName(ba.getSystemId(), QLT_INSP_FILE_FIELD_NAME).getDisplayName() + " Attachment and submit.");
		return params;
	}
	
	public static void fillStateTypes(QltObject qo,Hashtable<String,String> params)
	{
		ArrayList<String> list = new ArrayList<String>() ;
		list.add(qo.flowStatus.getName());
		params.put("enabledFS_value", gson.toJson(list));
		list.clear();
		list.add(qo.decision.getName());		
		params.put("enabledDec_value", gson.toJson(list) );
		list.clear();
		list.add(qo.pdfi.getName());
		params.put("enabledPDFI_value", gson.toJson(list) );
	}

	public static Hashtable<String,String> setPrefillTags( Hashtable<String,String> params, ArrayList<String> fs, ArrayList<String> dec, ArrayList<String> pdfi, Boolean genInsp, Boolean genMdcc, String msg) 
	{
		params.put("genInsp_checked", genInsp.toString());
		params.put("genMdcc_checked",genMdcc.toString());
		params.put("enabledFS_value", gson.toJson(fs));
		params.put("enabledPDFI_value", gson.toJson(pdfi));
		params.put("enabledDec_value",gson.toJson(dec));
		params.put("message_value", msg);
		return params;
	}
}
