package billtracking.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Utilities;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

/*
 * Responsible for navigating among states of a process
 */
public class StateProcessor implements IProcessRequestStructure,IState,IBillConstants,IBillProperties {

	private static final String NULL_USER = "null";
	private static final String USERLOGIN_SEPARATOR = ":";
	private static final String TYPE_STATE_DECISION_PENDING = "Pending";
	private static final String DECISION_FLOW_FORWARD = "forward";
	private static final Object MANUAL_ENTRY = "manual_entry";

	public void BringRequestToState(Request request, State state) {
		// TODO Auto-generated method stub

	}

	public void validateRequestToReachState(Request request, State state) {
		// TODO Auto-generated method stub

	}

	static void moveToRejState(Request currentRequest,
			Request oldRequest, boolean isAddRequest, Process process)
	throws DatabaseException, TBitsException {
		State rejState=process.getRejState(currentRequest);
		int id=0;
		State tempState;
		while(id!=rejState.getStateId()){
			tempState=moveToPrevState(currentRequest, oldRequest,
					isAddRequest,process);
			id=tempState.getStateId();
			if(id>100){
				throw new TBitsException("Configuration Error !! Check For State Id,Process Id:"
						+process.getState(currentRequest).getStateId()+process.getProcessId());
			}
		}
	}

	static void checkAttachments(Request currentRequest,
			State state) throws DatabaseException,
			TBitsException {
		String attFieldIds=state.get(state_attachment_ids);
		if(attFieldIds!=null){
			ArrayList<String> attids=Utilities.toArrayList(attFieldIds);

			if(attids!=null){
				for(String str:attids){
					int id=Integer.parseInt(str);
					Field f=Field.lookupBySystemIdAndFieldId(currentRequest.getSystemId(), id);
					boolean isChecked=(Boolean)currentRequest.getObject(f.getName());
					if(!isChecked)
						throw new TBitsException("Please Attach the Document: "+f.getDisplayName());

				}
			}
		}
	}

	public static void setFlowData(Request currentRequest, State state) {
		// TODO Auto-generated method stub

		currentRequest.setObject(bill_state_id,state.getStateId());
		currentRequest.setObject(bill_process_id,state.getProcessId());

	}

	//Set Dates of Current State
	public static void setRequestDates(Request currentRequest, State state) {
		// TODO Auto-generated method stub
		String duration=state.get(state_duration);
		Date loggedDate= currentRequest.getLoggedDate();
		String recDateField=state.get(state_Dep_Receipt_Date);
		String ackDateField=state.get(state_Dep_Acknowledge_Date);
		currentRequest.setObject(recDateField,loggedDate);
		currentRequest.setObject(ackDateField,loggedDate);
		Date targetDate;
		if(state.isLastState()){
			//Process process =Process.getProcess(currentRequest);
			//State firstStep=process.getFirstState(currentRequest);
			//String billReceiptDateField=firstStep.get(state_Dep_Receipt_Date);
			//Date billReceiptDate=(Date)currentRequest.getObject(billReceiptDateField);
			//targetDate = Utils.incrementTs(billReceiptDate,Integer.parseInt(BillProperties.get(PROPERTY_TOTAL_DURATION)));   
			targetDate=(Date)currentRequest.getObject("due_datetime");
			if(loggedDate.getTime()>targetDate.getTime())
				targetDate=loggedDate;
		}else{
			targetDate = Utils.incrementTs(loggedDate,Integer.parseInt(duration));
		}
		String tarDateField=state.get(state_target_date);
		currentRequest.setObject(tarDateField,targetDate);

	}

	public static void setStateStatusFields(Request currentRequest, State state) {
		// TODO Auto-generated method stub
		String pendingWith=state.get(state_pending_with);
		Type status;
		try {
			status = Type.lookupBySystemIdAndFieldNameAndTypeName(currentRequest.getSystemId(),Statuswithdepartments,pendingWith);
			currentRequest.setObject(Statuswithdepartments,status);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

	public static void setStateDecisionField(Request currentRequest,State state){
		if(state.isDecision()){
			Type pending;
			try {
				pending = Type.lookupAllBySystemIdAndFieldNameAndTypeName(currentRequest.getSystemId(),state.get(state_Decision_Field_Name),TYPE_STATE_DECISION_PENDING);
				currentRequest.setObject(state.get(state_Decision_Field_Name),pending);
			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static State moveToPrevState(Request currentRequest,
			Request oldRequest, boolean isAddRequest,Process process) throws DatabaseException,
			TBitsException {
		{
			int sysId=currentRequest.getSystemId();
			State state = process.getState(currentRequest);
			Boolean isFirstState = state.isFirstState();
			if(isFirstState)
			{
				Type closedStatus=Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysId,Statuswithdepartments,Type_Status_with_departments_Closed);
				currentRequest.setObject(Statuswithdepartments,closedStatus);
			}

			StateProcessor.resetFieldsOnRejection(currentRequest, state);
			State prevState = process.getPrevState(currentRequest);

			StateProcessor.setFlowData(currentRequest, prevState);
			StateProcessor.setRequestDates(currentRequest, prevState);
			StateProcessor.setStateStatusFields(currentRequest, prevState);
			StateProcessor.setStateDecisionField(currentRequest, prevState);
			StateProcessor.fillUserTypeFields(currentRequest,prevState);
			StateProcessor.fillTypeFields(currentRequest, prevState);
			return prevState;


		}
	}

	private static void resetFieldsOnRejection(Request currentRequest,
			State state) {
		currentRequest.setObject(state.get(state_Dep_Acknowledge_Date), null);
		currentRequest.setObject(state.get(state_Dep_Receipt_Date),null);
	}

	public static State moveToNextState(Request currentRequest,
			Request oldRequest, boolean isAddRequest, Process process)
	throws DatabaseException, TBitsException {
		int sysId=currentRequest.getSystemId();
		State state = process.getState(currentRequest);
		Boolean isLastState = state.isLastState();
		if(isLastState)
		{
			Type closedStatus=Type.lookupAllBySystemIdAndFieldNameAndTypeName(sysId,Statuswithdepartments,Type_Status_with_departments_Closed);
			currentRequest.setObject(Statuswithdepartments,closedStatus);
			return null;	
		}




		State nextState=process.getNextState(currentRequest);
		if(nextState==null){
			throw new TBitsException("unable to find next State for Current State id:"+state.getStateId());
		}
		StateProcessor.checkAttachments(currentRequest,nextState); 
		StateProcessor.setFlowData(currentRequest, nextState);
		StateProcessor.setStateStatusFields(currentRequest,nextState);
		StateProcessor.setRequestDates(currentRequest,nextState);
		StateProcessor.fillUserTypeFields(currentRequest,nextState);
		StateProcessor.fillTypeFields(currentRequest, nextState);

		return nextState;

	}

	static void fillUserTypeFields(Request currentRequest,State state) throws DatabaseException, TBitsException {
		String userTypeFieldNameString=state.get(state_usertype_field_names);
		String userTypeFieldValueString=state.get(state_usertype_field_values);

		if(userTypeFieldNameString!=null && userTypeFieldValueString!=null)
		{
			ArrayList<String> userTypeFieldNameList=Utilities.toArrayList(userTypeFieldNameString);
			ArrayList<String> userTypeFieldValueList=Utilities.toArrayList(userTypeFieldValueString);
			int size=userTypeFieldNameList.size();
			for(int index=0;index<size;index++){

				String userTypeLogin=userTypeFieldValueList.get(index);
				String userTypeFieldName=userTypeFieldNameList.get(index);

				if(userTypeLogin!=null&&userTypeLogin.equals(MANUAL_ENTRY)){
					Collection<RequestUser> manualUserEntry=(Collection<RequestUser>)currentRequest.getObject(userTypeFieldName);
					if(manualUserEntry==null){
						throw new TBitsException("Please Select User Name For :"+userTypeFieldName);
					}
				}

				if(userTypeLogin.equals(NULL_USER)){
					Collection<RequestUser> empty= new ArrayList<RequestUser>();
					currentRequest.setObject(userTypeFieldName,empty);
					continue;
				}


				ArrayList<String> userLoginList=Utilities.toArrayList(userTypeLogin,USERLOGIN_SEPARATOR);
				Collection<RequestUser> userTypeList = new ArrayList<RequestUser>();
				for(String usrLogin:userLoginList){
					User userTypeUser = User.lookupByUserLogin(usrLogin);
					Field f=Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(),userTypeFieldName);
					RequestUser ru = new  RequestUser(currentRequest.getSystemId(),currentRequest.getRequestId(),userTypeUser.getUserId(),1,false,f.getFieldId());
					userTypeList.add(ru);
				}
				currentRequest.setObject(userTypeFieldName,userTypeList);


			}
		}

	}
	static void fillTypeFields(Request currentRequest,State state) throws DatabaseException {
		String typeFieldName=state.get(state_type_field_names);
		String typeFieldValue=state.get(state_type_field_values);

		if(typeFieldName!=null && typeFieldValue!=null)
		{
			ArrayList<String> typeFieldNameList=Utilities.toArrayList(typeFieldName);
			ArrayList<String> typeFieldValueList=Utilities.toArrayList(typeFieldValue);
			int size=typeFieldNameList.size();
			for(int index=0;index<size;index++){
				Type type=Type.lookupAllBySystemIdAndFieldNameAndTypeName(currentRequest.getSystemId(), typeFieldNameList.get(index), typeFieldValueList.get(index));
				currentRequest.setObject(typeFieldNameList.get(index),type);

			}
		}

	}

	static String getManualFlowControlField(String decisionFlow,Process process,State state){

		Connection aCon;
		try {
			aCon = DataSourcePool.getConnection();
			PreparedStatement pstmt = aCon.prepareStatement("Select distinct(key_data) from plugins_decision_table  where process_id=? " +
			"and current_state_id=? and decision_flow like ?");
			pstmt.setInt(1,process.getProcessId());
			pstmt.setInt(2,state.getStateId());
			pstmt.setString(3,"%"+decisionFlow+"%");
			ResultSet rs = pstmt.executeQuery();
			String controlFieldName = null;
			while(rs!=null && rs.next()!=false){
				String keyData=rs.getString("key_data");
				controlFieldName=Utils.fieldTrimmer(keyData).trim();
			}
			rs.close();
			aCon.close();
			return controlFieldName;


		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;


	}	
	static State getNextState(String controlFieldName,String controlFieldValue,String decisionFlow,Process process,State state){
		List<Decision> decisionList=Decision.lookUpByProcessIdAndCurrentStateId(process.getProcessId(),state.getStateId());
		if(decisionList!=null){
			for(Decision dec:decisionList){
				if (dec.getKeyData().equals(controlFieldName)&&
						dec.getValueData().equals(controlFieldValue)&&
						dec.getDecisionFlow().equals(decisionFlow))
				{
					return process.getState(dec.getNextStateId());
				}

			}

		}
		return null;
	}


	static State getRejState(String controlFieldName,String controlFieldValue,String decisionFlow,Process process,State state){
		List<Decision> decisionList=Decision.lookUpByProcessIdAndCurrentStateId(process.getProcessId(),state.getStateId());
		if(decisionList!=null){
			for(Decision dec:decisionList){
				if (dec.getKeyData().equals(controlFieldName)&&
						dec.getValueData().equals(controlFieldValue)&&
						dec.getDecisionFlow().equals(decisionFlow))
				{
					return process.getState(dec.getRejectionStateId());
				}

			}

		}
		return null;
	}


	public static void main(String[] args) {
		transbit.tbits.domain.Request currentRequest;
		try {
			currentRequest = transbit.tbits.domain.Request.lookupBySystemIdAndRequestId(81,6);
			Process process=Process.getProcess(currentRequest);
			State state=process.getState(currentRequest);

			String controlFieldName=getManualFlowControlField(DECISION_FLOW_FORWARD, process, state);
			//			String controlFieldValue=currentRequest.get(controlFieldName);
			//			System.out.println(controlFieldValue);
			State nextState=getNextState(controlFieldName,"PPM", DECISION_FLOW_FORWARD, process,state);
			System.out.println(nextState.getStateId());
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
