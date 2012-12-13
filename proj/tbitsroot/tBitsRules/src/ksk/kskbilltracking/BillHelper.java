package kskbilltracking;

import static kskbilltracking.BillConstants.*;
import static kskbilltracking.BillProperties.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import java.util.Calendar;

public class BillHelper {
	public static void processAddState(Request currentRequest,Request oldRequest,boolean isAddRequest,int sysId) throws IllegalStateException, DatabaseException, TBitsException{

		nullCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

		Type status = Type.lookupBySystemIdAndFieldNameAndTypeName(sysId,Bill_Pending_With,Type_Pending_With_Document_Cell);
		currentRequest.setStatusId(status);

		currentRequest.setObject(Bill_Doc_Cell_Receipt, currentRequest.getLoggedDate());
		//setting KMPCL payment date
		Date scd = (Date)currentRequest.getObject(Bill_Doc_Cell_Receipt);
		Date pd = incrementTs(scd,Total_Duration);
		currentRequest.setObject(Bill_KMPCL_Payment_Date, pd);


		//setting duedate
		int processId=getProcessId(currentRequest);
		Hashtable<String,String> nextStepData = new Hashtable<String,String>();
		nextStepData=getActionTableByStepId(Integer.toString(processId),"1");
		String duration=nextStepData.get(Db_step_Duration);

		Date ndd= incrementTs(scd,Integer.parseInt(duration));
		currentRequest.setObject(Field.DUE_DATE,ndd);	

		//set assignee


		Collection<RequestUser> assList = currentRequest.getAssignees();
		Collection<RequestUser> logger=currentRequest.getLoggers();

		//int aSystemId, int aRequestId, int aUserId, int aOrdering, boolean aIsPrimary,int aFieldId
		Field f=Field.lookupBySystemIdAndFieldName(sysId, Field.ASSIGNEE);
		for(RequestUser lu:logger){
			RequestUser ru = new  RequestUser(lu.getSystemId(),lu.getRequestId(),lu.getUserId(),lu.getOrdering(),lu.getIsPrimary(),f.getFieldId());
			assList.add(ru);
		}
		currentRequest.setAssignees(assList);



	}





	public static void setAssignee(String UserLogin,Request currentRequest, int sysId) throws DatabaseException{
		User ass  = User.lookupByUserLogin(UserLogin);
		Field f = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), Field.ASSIGNEE);
		RequestUser ru = new  RequestUser(sysId,currentRequest.getRequestId(),ass.getUserId(),1,false,f.getFieldId());
		ArrayList<RequestUser> assList= new ArrayList<RequestUser>();
		assList.add(ru);
		currentRequest.setAssignees(assList);	
	}

	//if stores inititates the bill



	public static void diffCheckBasicFields(Request currentRequest,Request oldRequest,boolean isAddRequest, int sysId) throws IllegalStateException, DatabaseException, TBitsException{

		Type billIniAgency=currentRequest.getSeverityId();
		if(billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Procurement)){
			diffString(Bill_Invoice_Value,currentRequest,oldRequest);
			diffString(Bill_Invoice_No,currentRequest,oldRequest);
			diffDate(Bill_Invoice_Date,currentRequest,oldRequest);
			diffDate(Field.DUE_DATE,currentRequest,oldRequest);
			diffDate(Bill_KMPCL_Payment_Date,currentRequest,oldRequest);
			diffObject(Bill_Currency,currentRequest,oldRequest);
			diffType(Field.SEVERITY,currentRequest,oldRequest);
			diffObject(Field.ASSIGNEE,currentRequest,oldRequest);
			diffType(Field.CATEGORY,currentRequest,oldRequest);
			diffType(Field.REQUEST_TYPE,currentRequest,oldRequest);
			diffType(Field.STATUS,currentRequest,oldRequest);

		}else{
			diffString(Bill_Invoice_Value,currentRequest,oldRequest);
			diffString(Bill_Invoice_No,currentRequest,oldRequest);
			diffDate(Bill_Invoice_Date,currentRequest,oldRequest);
			diffDate(Field.DUE_DATE,currentRequest,oldRequest);
			diffDate(Bill_KMPCL_Payment_Date,currentRequest,oldRequest);
			diffObject(Bill_Currency,currentRequest,oldRequest);
			diffType(Field.SEVERITY,currentRequest,oldRequest);
			diffObject(Field.ASSIGNEE,currentRequest,oldRequest);
			diffString(Bill_GRN_No,currentRequest,oldRequest);
			diffDate(Bill_GRN_Date,currentRequest,oldRequest);
			diffString(Bill_MDCC_No,currentRequest,oldRequest);
			diffString(Bill_Deduction,currentRequest,oldRequest);
			diffString(Bill_Net_Invoice_Value,currentRequest,oldRequest);
			diffType(Field.CATEGORY,currentRequest,oldRequest);
			diffType(Field.REQUEST_TYPE,currentRequest,oldRequest);
			diffType(Field.STATUS,currentRequest,oldRequest);
		}
	}

	public static void nullCheckBasicFields(Request currentRequest , Request oldRequest, boolean isAddRequest, int sysId) 
	throws IllegalStateException, DatabaseException, TBitsException{

		String mandateFieldsSCM="Please Fill All Mandatory Fields:<br> vendor,contract type,currency type<br>"+
		"Invoice No,Invoice Value,Invoice Date<br>";

		String mandateFieldsStores="Please Fill All Mandatory Fields:<br>" +
		"vendor,contract type,currency type<br>"+
		"Invoice No,Invoice Value,Invoice Date<br>"+
		"Grn No,Grn Date,MDCC No<br>"+
		"Deduction,Net Invoice Value";


		Type billIniAgency=currentRequest.getSeverityId();
		if(billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Procurement)){
			//checking basic values for null
			boolean eval = currentRequest.get(Bill_Invoice_Value)==null || 
			//currentRequest.get(Bill_Invoice_Value).equals("")||
			currentRequest.get(Bill_Invoice_No)==null||
			//currentRequest.get(Bill_Invoice_No).equals("")||
			currentRequest.getObject(Bill_Invoice_Date)==null;
			if(eval)
				throw new TBitsException(mandateFieldsSCM);

			if(currentRequest.getAttachments().isEmpty()){

				throw new TBitsException("Please Attach the Scanned copy of the Bill under Attachments");

			}


		}else{
			boolean eval = currentRequest.get(Bill_Invoice_Value)==null || 
			//currentRequest.get(Bill_Invoice_Value).equals("")||
			currentRequest.get(Bill_Invoice_No)==null||
			//currentRequest.get(Bill_Invoice_No).equals("")||
			currentRequest.getObject(Bill_Invoice_Date)==null||
			//	currentRequest.getObject(Bill_Stores_Receipt)==null||
			currentRequest.get(Bill_GRN_No)==null||
			//currentRequest.get(Bill_GRN_No).equals("")||
			currentRequest.getObject(Bill_GRN_Date)==null||
			currentRequest.get(Bill_MDCC_No)==null||
			//currentRequest.get(Bill_MDCC_No).equals("")||
			currentRequest.get(Bill_Deduction)==null||
			//currentRequest.get(Bill_Deduction).equals("")||
			currentRequest.get(Bill_Net_Invoice_Value)==null;
			//currentRequest.get(Bill_Net_Invoice_Value).equals("");
			if(eval)
				throw new TBitsException(mandateFieldsStores);

			if(currentRequest.getAttachments().isEmpty()){

				throw new TBitsException("Please Attach the Scanned copy of the Bill under Attachments");

			}


		}
	}



	public static void processUpdateState( Request currentRequest,Request oldRequest ,boolean isAddRequest,int sysId) throws Exception{

		int processId =getProcessId(oldRequest);
		int currStepId=getStepId(oldRequest);

		Hashtable<String,Hashtable<String,String>>allStepsHash = new Hashtable<String,Hashtable<String,String>>();
		allStepsHash=getActionTableByAllSteps(processId);


		switch(currStepId){
		case 1: processFirstStep(currentRequest, oldRequest, isAddRequest, sysId, allStepsHash);
		break;
		case 2:
		case 3:
		case 4:
		case 5:
		case 6: processStep(currStepId, currentRequest, oldRequest, isAddRequest, sysId, allStepsHash);
		break;

		case 7: processLastStep(currentRequest, oldRequest, isAddRequest, sysId, allStepsHash);
		break;
		case 8: throw new TBitsException("You cannot Update a Closed Request");		

		default: throw new TBitsException("invalid State");
		}


	}


	public static void processFirstStep(Request currentRequest,Request oldRequest,boolean isAddRequest,int sysId,
			Hashtable<String,Hashtable<String,String>>allStepsHash) throws IllegalStateException, DatabaseException, TBitsException{

		diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

		diffDate(Bill_Doc_Cell_Receipt, currentRequest, oldRequest);


		Date ackDate=(Date)currentRequest.getObject(Bill_Doc_Cell_Acknowledgement);
		if(ackDate==null)
			throw new TBitsException("Please Fill Doc Cell Acknowledgement Date");


		Hashtable<String,String> currStepData=allStepsHash.get("1");

		int nextStepId=getNextStepId(1, allStepsHash);
		Hashtable<String,String> nextStepData=allStepsHash.get(Integer.toString(nextStepId));

		//setting Receipt date of department from step2
		currentRequest.setObject(nextStepData.get(Db_step_Dep_Receipt_Date), ackDate);

		//setting the Due Date for  step_id 2			
		Date ndd=incrementTs(ackDate,Integer.parseInt(nextStepData.get(Db_step_Duration)));
		Date klpd = (Date)currentRequest.getObject(Bill_KMPCL_Payment_Date);

		if(ndd.getTime()>klpd.getTime()){
			currentRequest.setDueDate(klpd);  
		}
		else currentRequest.setDueDate(ndd);   

		Calendar cur = Calendar.getInstance(); 
		if(cur.getTimeInMillis()>ndd.getTime()){
			//cce'd dep heads
			User cc1 = User.lookupByUserLogin(currStepData.get(Db_step_Dep_Head));
			Field f=Field.lookupBySystemIdAndFieldName(sysId, Field.CC);
			RequestUser ru = new  RequestUser(sysId,currentRequest.getRequestId(),cc1.getUserId(),1,false,f.getFieldId());

			User cc2 = User.lookupByUserLogin(nextStepData.get(Db_step_Dep_Head));
			RequestUser ru1 = new  RequestUser(sysId,currentRequest.getRequestId(),cc2.getUserId(),2,false,f.getFieldId());

			Collection<RequestUser> ccList = new ArrayList<RequestUser>();
			ccList.add(ru);
			ccList.add(ru1);
			currentRequest.setCcs(ccList);

			if (currentRequest.getDescription().equalsIgnoreCase("")||currentRequest.getDescription()==null)
			{
				throw new TBitsException("Please Fill the reason for delay in description");
			}

		}

		Type pendingWith=Type.lookupBySystemIdAndFieldNameAndTypeName(sysId,Bill_Pending_With,nextStepData.get(Db_step_pending_with));
		currentRequest.setStatusId(pendingWith);

		//setting assignee to Budgeting head
		User ass = null;
		try {
			ass = User.lookupByUserLogin(nextStepData.get(Db_step_Dep_Head));
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Field f=Field.lookupBySystemIdAndFieldName(sysId, Field.ASSIGNEE);
		RequestUser rub = new  RequestUser(sysId,currentRequest.getRequestId(),ass.getUserId(),1,false,f.getFieldId());

		Collection<RequestUser> assList=new ArrayList<RequestUser>();
		assList.add(rub);
		currentRequest.setAssignees(assList);

	}


	public static int getNextStepId(int currentStepId,Hashtable<String,Hashtable<String,String>>allStepsHash){

		if(currentStepId==7) return -1;
		else{
			int nextStepId=currentStepId+1;
			Hashtable<String,String> nextStepData = new Hashtable<String,String>();
			nextStepData=allStepsHash.get(Integer.toString(nextStepId));
			while(nextStepData.get(Db_step_Duration).equals("0"))
				nextStepData=allStepsHash.get(Integer.toString(++nextStepId));
			return nextStepId;
		}
	}

	public static int getPrevStepId(int currentStepId,Hashtable<String,Hashtable<String,String>>allStepsHash){
		if(currentStepId==1) return -1;
		else{
			int prevStepId=currentStepId-1;
			Hashtable<String,String> prevStepData = new Hashtable<String,String>();
			prevStepData=allStepsHash.get(Integer.toString(prevStepId));
			while(prevStepData.get(Db_step_Duration).equals("0"))
				prevStepData=allStepsHash.get(Integer.toString(--prevStepId));
			return prevStepId;
		}

	}

	public static void processStep(int currStepId,Request currentRequest,Request oldRequest,boolean isAddRequest,int sysId,
			Hashtable<String,Hashtable<String,String>>allStepsHash) throws IllegalStateException, DatabaseException, TBitsException{

		Hashtable<String,String> currStepData=allStepsHash.get(Integer.toString(currStepId));

		int nextStepId=getNextStepId(currStepId, allStepsHash);
		Hashtable<String,String> nextStepData =allStepsHash.get(Integer.toString(nextStepId));

		int prevStepId=getPrevStepId(currStepId, allStepsHash);
		Hashtable<String,String> prevStepData =allStepsHash.get(Integer.toString(prevStepId));

		Type stepDecision = (Type)currentRequest.getObject(currStepData.get(Db_step_Decision));

		//processing after budgeting decision is made

		if(stepDecision.getName().equals("Pending"))
		{
			diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

			diffCheckDownLineFields(currentRequest,oldRequest,isAddRequest,sysId,prevStepId,allStepsHash);

			diffDate(currStepData.get(Db_step_Dep_Receipt_Date),currentRequest,oldRequest);
		}

		else
			if(stepDecision.getName().equals("Approved"))
			{
				diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

				diffCheckDownLineFields(currentRequest,oldRequest,isAddRequest,sysId,prevStepId,allStepsHash);

				diffDate(currStepData.get(Db_step_Dep_Receipt_Date),currentRequest,oldRequest);







				Date ackDate=(Date)currentRequest.getObject(currStepData.get(Db_step_Dep_Acknowledge_Date));
				if(ackDate==null){

					Field field = Field.lookupBySystemIdAndFieldName(sysId,currStepData.get(Db_step_Dep_Acknowledge_Date));
					throw new TBitsException("Please Fill " + field.getDisplayName());
				}
				if(currStepId==6)
				{
					if(!(Boolean)currentRequest.getObject(Bill_Hard_Copy_Received))
						throw new TBitsException("Please Check Whether Hard Copy Received");
				}

				//setting Receipt date of department of next step
				currentRequest.setObject(nextStepData.get(Db_step_Dep_Receipt_Date), ackDate);




				Calendar cur = Calendar.getInstance(); 
				long currDueDatesec=currentRequest.getDueDate().getTime();
				long currTimesec=cur.getTimeInMillis();

				if(currTimesec>currDueDatesec){
					//cce'd dep heads


					User cc1 = User.lookupByUserLogin(currStepData.get(Db_step_Dep_Head));
					Field f=Field.lookupBySystemIdAndFieldName(sysId, Field.CC);
					RequestUser ru = new  RequestUser(sysId,currentRequest.getRequestId(),cc1.getUserId(),1,false,f.getFieldId());

					User cc2 = User.lookupByUserLogin(nextStepData.get(Db_step_Dep_Head));
					RequestUser ru1 = new  RequestUser(sysId,currentRequest.getRequestId(),cc2.getUserId(),2,false,f.getFieldId());

					Collection<RequestUser> ccList = new ArrayList<RequestUser>();
					ccList.add(ru);
					ccList.add(ru1);
					currentRequest.setCcs(ccList);

					if (currentRequest.getDescription().equalsIgnoreCase("")||currentRequest.getDescription()==null)
					{
						throw new TBitsException("Please Fill the reason for delay in description");
					}


				}

				//setting the Due Date of department of next step				
				Date ndd=incrementTs(ackDate,Integer.parseInt(currStepData.get(Db_step_Duration)));
				Date klpd = (Date)currentRequest.getObject(Bill_KMPCL_Payment_Date);

				if(ndd.getTime()>klpd.getTime()){
					currentRequest.setDueDate(klpd);  
				}
				else currentRequest.setDueDate(ndd);   


				Type pendingWith=Type.lookupBySystemIdAndFieldNameAndTypeName(sysId,Bill_Pending_With,nextStepData.get(Db_step_pending_with));
				currentRequest.setStatusId(pendingWith);

				//setting assignee of next step department
				User ass = null;
				try {
					ass = User.lookupByUserLogin(nextStepData.get(Db_step_Dep_Head));
				} catch (DatabaseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}


				Field f = Field.lookupBySystemIdAndFieldName(sysId, Field.ASSIGNEE);
				RequestUser rub = new  RequestUser(sysId,currentRequest.getRequestId(),ass.getUserId(),1,false,f.getFieldId());

				Collection<RequestUser> assList= new ArrayList<RequestUser>();
				assList.add(rub);
				currentRequest.setAssignees(assList);



			}
			else 
				if(stepDecision.getName().equals("Rejected"))
				{
					{

						diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

						diffCheckDownLineFields(currentRequest,oldRequest,isAddRequest,sysId,prevStepId,allStepsHash);

						currentRequest.setObject(currStepData.get(Db_step_Dep_Acknowledge_Date), null);
						currentRequest.setObject(currStepData.get(Db_step_Dep_Receipt_Date),null);

						currentRequest.setObject(prevStepData.get(Db_step_Dep_Acknowledge_Date), null);
						currentRequest.setObject(prevStepData.get(Db_step_Dep_Receipt_Date),currentRequest.getLoggedDate());

						//there is no such field as decision in user doc dep
						if(prevStepId!=1)
							currentRequest.setObject(prevStepData.get(Db_step_Decision), "Pending");

						Date ndd = incrementTs(currentRequest.getLoggedDate(), 
								Integer.parseInt(prevStepData.get(Db_step_Duration)));


						Date klpd = (Date)currentRequest.getObject(Bill_KMPCL_Payment_Date);

						if(ndd.getTime()>klpd.getTime()){
							currentRequest.setDueDate(klpd);  
						}
						else currentRequest.setDueDate(ndd);   

						Type pendingWith=Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, Bill_Pending_With,prevStepData.get(Db_step_pending_with));
						currentRequest.setStatusId(pendingWith);
						setAssignee(prevStepData.get(Db_step_Dep_Head), currentRequest, sysId);currentRequest.setObject(Bill_Finance_Payment, null);

					}

				}
	}




	public static void processLastStep(Request currentRequest,
			Request oldRequest, boolean isAddRequest, int sysId,
			Hashtable<String, Hashtable<String, String>> allStepsHash) throws IllegalStateException, DatabaseException, TBitsException {
		// TODO Auto-generated method stub

		Hashtable<String,String> currStepData=allStepsHash.get(Integer.toString(7));
		Type stepDecision = (Type)currentRequest.getObject(currStepData.get(Db_step_Decision));
		int prevStepId=getPrevStepId(7, allStepsHash);
		Hashtable<String,String> prevStepData=allStepsHash.get(Integer.toString(prevStepId));


		if(stepDecision.getName().equals("Pending"))
		{
			diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

			diffCheckDownLineFields(currentRequest,oldRequest,isAddRequest,sysId,prevStepId,allStepsHash);

			diffDate(currStepData.get(Db_step_Dep_Receipt_Date),currentRequest,oldRequest);
		}

		else
			if(stepDecision.getName().equals("Approved"))
			{
				diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

				diffCheckDownLineFields(currentRequest,oldRequest,isAddRequest,sysId,prevStepId,allStepsHash);

				diffDate(currStepData.get(Db_step_Dep_Receipt_Date),currentRequest,oldRequest);


				Type pendingWithclosed=Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, Bill_Pending_With,Type_Pending_With_Closed);
				currentRequest.setStatusId(pendingWithclosed); 
				Date fd=currentRequest.getDueDate();
				Date kd=(Date)currentRequest.getObject(Bill_KMPCL_Payment_Date);
				if(fd.getTime() > kd.getTime()){
					if(currentRequest.getDescription()==null || currentRequest.getDescription().equals(""))
						throw new TBitsException("Please Fill the Reason for delay in description");
					currentRequest.setDueDate(kd);

				}

			}
			else 
				if(stepDecision.getName().equals("Rejected"))
				{

					diffCheckBasicFields(currentRequest, oldRequest, isAddRequest, sysId);

					diffCheckDownLineFields(currentRequest,oldRequest,isAddRequest,sysId,prevStepId,allStepsHash);

					currentRequest.setObject(currStepData.get(Db_step_Dep_Acknowledge_Date), null);
					currentRequest.setObject(currStepData.get(Db_step_Dep_Receipt_Date),currentRequest.getLoggedDate());

					currentRequest.setObject(prevStepData.get(Db_step_Dep_Acknowledge_Date), null);
					currentRequest.setObject(prevStepData.get(Db_step_Dep_Receipt_Date),currentRequest.getLoggedDate());
					currentRequest.setObject(prevStepData.get(Db_step_Decision), "Pending");

					Date ndd = incrementTs(currentRequest.getLoggedDate(), 
							Integer.parseInt(prevStepData.get(Db_step_Duration)));


					Date klpd = (Date)currentRequest.getObject(Bill_KMPCL_Payment_Date);

					if(ndd.getTime()>klpd.getTime()){
						currentRequest.setDueDate(klpd);  
					}
					else currentRequest.setDueDate(ndd);   

					Type pendingWith=Type.lookupBySystemIdAndFieldNameAndTypeName(sysId, Bill_Pending_With,prevStepData.get(Db_step_pending_with));
					currentRequest.setStatusId(pendingWith);
					setAssignee(prevStepData.get(Db_step_Dep_Head), currentRequest, sysId);currentRequest.setObject(Bill_Finance_Payment, null);





				}

	}





	public static void diffDate(String fieldName,Request currentRequest,Request oldRequest) throws IllegalStateException, DatabaseException, TBitsException{
		boolean eval =Timestamp.toCustomFormat((Date)currentRequest.getObject(fieldName),"yyyy-MM-dd")
		.equals(Timestamp.toCustomFormat((Date)oldRequest.getObject(fieldName),"yyyy-MM-dd"));
		if(!eval)
		{
			Field field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
			throw new TBitsException("You Cannot Change "+field.getDisplayName());
		}

	}

	public static void diffType(String fieldName,Request currentRequest,Request oldRequest) throws IllegalStateException, DatabaseException, TBitsException{
		boolean eval =currentRequest.getObject(fieldName).equals(oldRequest.getObject(fieldName));
		if(!eval)
		{
			Field field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
			throw new TBitsException("You Cannot Change "+field.getDisplayName());
		}

	}


	public static void diffObject(String fieldName,Request currentRequest,Request oldRequest) throws IllegalStateException, DatabaseException, TBitsException{
		boolean eval =currentRequest.getObject(fieldName).equals(oldRequest.getObject(fieldName));
		if(!eval)
		{
			Field field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
			throw new TBitsException("You Cannot Change "+field.getDisplayName());
		}

	}


	public static void diffString(String fieldName,Request currentRequest,Request oldRequest) throws IllegalStateException, DatabaseException, TBitsException{
		boolean eval =currentRequest.get(fieldName).equals(oldRequest.get(fieldName));
		if(!eval)
		{
			Field field = Field.lookupBySystemIdAndFieldName(currentRequest.getSystemId(), fieldName);
			throw new TBitsException("You Cannot Change "+field.getDisplayName());
		}

	}



	public static void diffCheckSwitch(Request currentRequest,Request oldRequest,int currentStepId) throws IllegalStateException, DatabaseException, TBitsException{

		switch(currentStepId){


		case 7:  diffDate(Bill_Finance_Payment,currentRequest,oldRequest); 
		diffDate(Bill_Finance_Received,currentRequest,oldRequest);
		diffType(Bill_Finance_Decision, currentRequest, oldRequest);
		break;
		case 6:  diffDate(Bill_Budgeting_Acknowledgement,currentRequest,oldRequest);
		diffDate(Bill_Budgeting_Receipt,currentRequest,oldRequest);
		diffType(Bill_Budgeting_Decision, currentRequest, oldRequest);
		break;
		case 5:  diffDate(Bill_SCM_Acknowledgement,currentRequest,oldRequest);
		diffDate(Bill_SCM_Receipt,currentRequest,oldRequest);
		diffType(Bill_SCM_Decision, currentRequest, oldRequest);
		break;
		case 4:  diffDate(Bill_Site_Head_Acknowledgement,currentRequest,oldRequest);
		diffDate(Bill_Site_Head_Receipt,currentRequest,oldRequest);
		diffType(Bill_Site_Head_Decision, currentRequest, oldRequest);

		break;
		case 3:  diffDate(Bill_User_Dept_Acknowledgement,currentRequest,oldRequest); 
		diffDate(Bill_User_Dept_Receipt,currentRequest,oldRequest);
		diffType(Bill_User_Dept_Decision, currentRequest, oldRequest);
		break;
		case 2:  diffDate(Bill_Stores_Acknowledgement,currentRequest,oldRequest);
		diffDate(Bill_Stores_Receipt,currentRequest,oldRequest);
		diffType(Bill_Stores_Decision, currentRequest, oldRequest);
		break;
		case 1:  diffDate(Bill_Doc_Cell_Acknowledgement,currentRequest,oldRequest);
		diffDate(Bill_Doc_Cell_Receipt,currentRequest,oldRequest);
		break;	
		default: throw new TBitsException("Invalid Step");

		}
	}

	public static void diffCheckDownLineFields(Request currentRequest,
			Request oldRequest, boolean isAddRequest, int sysId, int currentStepId,Hashtable<String,Hashtable<String,String>>allStepsHash) throws IllegalStateException, DatabaseException, TBitsException {
		while(currentStepId!=-1){
			diffCheckSwitch(currentRequest, oldRequest, currentStepId);
			currentStepId=getPrevStepId(currentStepId, allStepsHash); 
		}
	}




	public static int getProcessId(Request request){

		Type billIniAgency = request.getSeverityId();
		Type contractType =  request.getRequestTypeId();

		if(contractType.getName().equals(Type_Contract_Type_Offshore_Supply) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Stores)){
			return 1;
		}else if(contractType.getName().equals(Type_Contract_Type_Offshore_Supply) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Procurement)){
			return 2;
		}else if(contractType.getName().equals(Type_Contract_Type_Onshore_Supply) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Stores)){
			return 3;
		}else if(contractType.getName().equals(Type_Contract_Type_Onshore_Supply) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Procurement)){
			return 4;
		}else if(contractType.getName().equals(Type_Contract_Type_Offshore_Services) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Procurement)){
			return 5;
		}else if(contractType.getName().equals(Type_Contract_Type_Onshore_Services) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Stores)){
			return 6;
		}else if(contractType.getName().equals(Type_Contract_Type_Onshore_Services) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Procurement)){
			return 7;
		}else if(contractType.getName().equals(Type_Contract_Type_Construction) &&
				billIniAgency.getName().equals(Type_Bill_Initiation_Agency_KMPCL_Stores)){
			return 8;
		} else 
			return 0;



	}

	public static int getStepId(Request oldRequest){

		Type pendingWith = oldRequest.getStatusId();

		if(pendingWith.getName().equals(Type_Pending_With_Document_Cell))
			return 1;
		else if(pendingWith.getName().equals(Type_Pending_With_Stores))
			return 2;
		else if(pendingWith.getName().equals(Type_Pending_With_User_Department))
			return 3;
		else if(pendingWith.getName().equals(Type_Pending_With_Site_Head))
			return 4;
		else if(pendingWith.getName().equals(Type_Pending_With_Procurement))
			return 5;
		else if(pendingWith.getName().equals(Type_Pending_With_Budgeting))
			return 6;
		else if(pendingWith.getName().equals(Type_Pending_With_Finance___Accounts))
			return 7;
		else if(pendingWith.getName().equals(Type_Pending_With_Closed))
			return 8;
		else
			return 0;

	}

	public static Date incrementTs(Date da,int noOfDays){
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(da.getTime());
		cal.add(Calendar.DAY_OF_MONTH,noOfDays);
		Date nda=new Date(cal.getTimeInMillis());
		return nda;
	}



}


