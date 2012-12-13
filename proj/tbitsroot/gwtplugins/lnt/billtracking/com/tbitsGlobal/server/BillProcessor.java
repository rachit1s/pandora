package billtracking.com.tbitsGlobal.server;


import static billtracking.com.tbitsGlobal.server.BillProperties.billProperties;

import java.util.Date;
import java.util.HashMap;

import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;
import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;
import billtracking.com.tbitsGlobal.shared.IGRNConstants;
import billtracking.com.tbitsGlobal.shared.IPOConstants;

public class BillProcessor implements IState,IBillProperties,IBillConstants,IPOConstants,IGRNConstants {



	private static Request poRequest=null;
	private static Request grnRequest=null;

	static private Request getLinkedRequestByBa(Request currentRequest,String sysPrefix) throws TbitsExceptionClient, TBitsException 
		{				
			try {
				String srcSysPrefix = null;
				BusinessArea srcBA = null;
				String relatedRequests = currentRequest.getRelatedRequests();

				if ((relatedRequests != null) && (relatedRequests.trim().length() != 0)){
					String[] srcRequestsSmartTags = relatedRequests.split(",");
					if (srcRequestsSmartTags != null){						
						for (String srcReqSmartTag : srcRequestsSmartTags){

							if(srcReqSmartTag != null){
								int requestId = 0;
								String[] part = srcReqSmartTag.split("#");
								if (part != null){

									srcSysPrefix = part[0];
									srcBA = BusinessArea.lookupBySystemPrefix(srcSysPrefix);
									if (srcBA == null)
										throw new TbitsExceptionClient("Invalid business area: " + srcSysPrefix);

									requestId = Integer.parseInt(part[1]);

									if (requestId > 0){
										if(srcSysPrefix.equals(sysPrefix)){
											Request req = Request.lookupBySystemIdAndRequestId(srcBA.getSystemId(), requestId);
											return req;
										}

									}

								}
							}
						}
					}
				}

			} catch (DatabaseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
		
	

	public static void processAddBill(Request currentRequest,Request oldRequest,boolean isAddRequest) throws IllegalStateException, DatabaseException, TbitsExceptionClient, TBitsException{
		rejectActiveBill(currentRequest);
		fillPOData(currentRequest);
		fillGRNData(currentRequest);
		generateAndSetUniqueField(currentRequest);
		
		//set request as private
		currentRequest.setIsPrivate(true);

		//determine Process ID
		//set the other values with regards to that
		Process process = Process.getProcess(currentRequest);
		int processId=process.getProcessId();
		if(processId==0)
			throw new TbitsExceptionClient("invalid Process Id");

		State firstState=process.getFirstState(currentRequest);
		//StateProcessor.checkAttachments(currentRequest,firstState);
		StateProcessor.setFlowData(currentRequest, firstState);
		StateProcessor.setStateStatusFields(currentRequest, firstState);
		StateProcessor.setStateDecisionField(currentRequest, firstState);
		StateProcessor.setRequestDates(currentRequest, firstState);
		StateProcessor.fillUserTypeFields(currentRequest,firstState);
		StateProcessor.fillTypeFields(currentRequest, firstState);


	}

	private static void generateAndSetUniqueField(Request currentRequest) {
		String grnno=currentRequest.get(GRN_);
		String invoiceno=currentRequest.get(Invoice_);
		String grnnoinvoiceno=grnno+"####"+invoiceno;
		currentRequest.setObject(GRN_Invoice_,grnnoinvoiceno);
	}
	private static void fillGRNData(Request currentRequest) throws TbitsExceptionClient, TBitsException {
		String grnPrefix=billProperties.get(PROPERTY_GRN_BA_PREFIX);
		grnRequest = getLinkedRequestByBa(currentRequest, grnPrefix);
		if(grnRequest!=null){
			currentRequest.setObject(GRNofVendor,(String)grnRequest.getObject(GRNSESNo));
			currentRequest.setObject(GRNDate,(Date)grnRequest.getObject("GRNSEScreateddate"));
		}

	}

	private static void fillPOData(Request currentRequest) throws TbitsExceptionClient, TBitsException {
		String poPrefix=billProperties.get(PROPERTY_PO_BA_PREFIX);
		System.out.println(currentRequest.get(Field.RELATED_REQUESTS));
		poRequest = getLinkedRequestByBa(currentRequest, poPrefix);
		if(poRequest!=null){
			currentRequest.setObject(POnumber,(String)poRequest.getObject(POnumber));
			currentRequest.setObject(POdate,(Date)poRequest.getObject(POdate));
			currentRequest.setObject(POvalue,(Double)poRequest.getObject(POvalue));
			currentRequest.setObject(Materialdescription,(String)poRequest.get(Materialdescription));	
			currentRequest.setObject("VendorName",(String)poRequest.getObject("VendorName"));
			currentRequest.setObject("VendorCode",(String)poRequest.getObject("VendorCode" ));
			currentRequest.setObject("AmendmentNo",(String)poRequest.getObject("AmendmentNo"));
			 
		}
	}
	private static void rejectActiveBill(Request currentRequest)
	throws TbitsExceptionClient, TBitsException {
		String subPrefix=billProperties.get(PROPERTY_SUBMISSION_BA_PREFIX);
		Request submissionRequest=getLinkedRequestByBa(currentRequest, subPrefix);
		if(submissionRequest==null){
			throw new TbitsExceptionClient("You can Log Request from Submission BA Only");

		}else{
			String billStatus=submissionRequest.getStatusId().getName();
			if(billStatus.equalsIgnoreCase("Active")){
				throw new TbitsExceptionClient("This Bill is already Active");
			}
		}
	}

	public static void processUpdateBill(Request currentRequest,Request oldRequest ,boolean isAddRequest) throws Exception{
		fillPOData(currentRequest);
		fillGRNData(currentRequest);
		generateAndSetUniqueField(currentRequest);
		Process process = Process.getProcess(currentRequest);
		State state = process.getState(currentRequest);
		BillProcessor.preUpdateChecks(currentRequest,state);	

		String isDecision=state.get(is_state_decision);
		if(isDecision.equalsIgnoreCase("true")){
			Type stateDecision = (Type)currentRequest.getObject(state.get(state_Decision_Field_Name));
			if(stateDecision.getName().equals("Approved"))
			{

				StateProcessor.moveToNextState(currentRequest,oldRequest,isAddRequest,process);
			}
			else 
				if(stateDecision.getName().equals("Rejected"))
				{
					StateProcessor.moveToRejState(currentRequest, oldRequest, isAddRequest,
							process);
				}
		}else{
			StateProcessor.moveToNextState(currentRequest, oldRequest, 
					isAddRequest,process);
		}


	}

	public static void preUpdateChecks(Request currentRequest, State state) throws TBitsException, DatabaseException, TbitsExceptionClient {
		// TODO Auto-generated method stub
	
		
		Type status=(Type)currentRequest.getObject(Statuswithdepartments);
		boolean isClosed=status.getName().equals(Type_Status_with_departments_Closed);
		if(isClosed){
			throw new TBitsException("Bill Has been closed You cannot Update");
	
		}
	
	
		//rule 0
		BusinessArea ba = BusinessArea.lookupBySystemId(currentRequest.getSystemId()); 
		if(state.isFirstState()){		
			String pono = (String) currentRequest.getObject(PO_);
			if(pono==null||pono.isEmpty()){
				throw new TBitsException(" PO# should not be Empty");
			}
		}
		
		//rule1
		BusinessArea ba3 = BusinessArea.lookupBySystemId(currentRequest.getSystemId());
		if(state.isFirstState()){	
			String AppCer = currentRequest.get("ExecEntry");
			String dbapprover = currentRequest.get("ApproverDB");
			if((AppCer.equals("ApproverDecWithClaim")||AppCer.equals("ApproverDecWithoutClaim"))
				&&((dbapprover==null)||(dbapprover.length()==0))){
				throw new TBitsException("Please sepcify the Approver name");
			}
		}

		//rule 2
		BusinessArea ba1 = BusinessArea.lookupBySystemId(currentRequest.getSystemId()); 
		if(state.isSecondState()){		
			String grnno = (String) currentRequest.getObject(GRN_);
			String appdec = currentRequest.get(ExPMReco);
			if((grnno==null||grnno.isEmpty()) && (appdec.equals("Approved"))){
				throw new TBitsException(" GRNSESDPR# should not be Empty");
			}
		}
			//rule 3
		BusinessArea ba2 = BusinessArea.lookupBySystemId(currentRequest.getSystemId()); 
		if(state.isThirdState()){		
			String grnno1 = (String) currentRequest.getObject(GRN_);
			String appdec1 = currentRequest.get(ExPMReco);
			if((grnno1==null||grnno1.isEmpty()) && (appdec1.equals("Approved"))){
				throw new TBitsException(" GRNSESDPR# should not be Empty");
			}
		}
		
		//rule4
			 ba3 = BusinessArea.lookupBySystemId(currentRequest.getSystemId());
		if(state.isFirstState()){	
			String AppCer1 = currentRequest.get("ExecEntry");
			String dbcertifier = currentRequest.get("CertifierDB");
			if((AppCer1.equals("ApproverDecNtReq")||AppCer1.equals("ApproverDecNtReqWithClaim"))
				&&((dbcertifier==null)||(dbcertifier.length()==0))){
				throw new TBitsException("Please sepcify the Certifier name");
			}
		}
		
		//rule5
			 ba3 = BusinessArea.lookupBySystemId(currentRequest.getSystemId());
		if(state.isSecondState()||state.isThirdState()){	
			String AppCer2 = currentRequest.get("ExecEntry");
			String dbcertifier1 = currentRequest.get("CertifierDB");
			if((AppCer2.equals("ApproverDecWithClaim")||AppCer2.equals("ApproverDecWithoutClaim"))
				&&((dbcertifier1==null)||(dbcertifier1.length()==0))){
				throw new TBitsException("Please sepcify the Certifier name");
			}
		}

		//rule 6
		HashMap<String,String> relationMap= new HashMap<String,String>();
		relationMap.put("Payreq","Rpayreq");
		relationMap.put("Accpo","RAccpo");
		relationMap.put("Invoice","RInvoice");
		relationMap.put("Taxinv","RTaxinv");
		relationMap.put("Lrcopy","RLrcopy");
		relationMap.put("Packlist","RPacklist");
		relationMap.put("Deliverychelan","RDeliverychelan");
		relationMap.put("Acklrcopy","RAcklrcopy");
		relationMap.put("Siterptnote","RSiterptnote");
		relationMap.put("mrir","Rmrir");
		relationMap.put("Sitegrn","RSitegrn");
		relationMap.put("Serviceentrysheet","RServiceentrysheet");
		relationMap.put("Measurement","RMeasurement");
		relationMap.put("Attendsheet","RAttendsheet");
		relationMap.put("Vendortools","RVendortools");
		relationMap.put("Workmaninsurence","RWorkmaninsurence");
		relationMap.put("Specsitems","RSpecsitems");
		relationMap.put("Drawings","RDrawings");
		relationMap.put("pocopy","Rpocopy");
		relationMap.put("Dispatchdoc","RDispatchdoc");
		relationMap.put("grn","Rgrn");
		relationMap.put("Testcerti","RTestcerti");
		relationMap.put("Inspectioncerti","RInspectioncerti");
		relationMap.put("Inspectionreportcopy","RInspectionreportcopy");
		relationMap.put("Commissioningprotocolcopy","RCommissioningprotocolcopy");
		relationMap.put("Guaranteeprotocolcopy","RGuaranteeprotocolcopy");
		relationMap.put("Dispatchcerti","RDispatchcerti");
		relationMap.put("Insurencecopy","RInsurencecopy");
		relationMap.put("Submissioncerti","RSubmissioncerti");
		relationMap.put("Noclaimscerti","RNoclaimscerti");
		relationMap.put("gdoc1","Rgdoc1");
		relationMap.put("gdoc2","Rgdoc2");
		relationMap.put("gdoc3","Rgdoc3");
		relationMap.put("gdoc4","Rgdoc4");
		relationMap.put("Otherdocuments","ROtherdocuments");
		relationMap.put("eALPSprintout","ReALPSprintout");
		relationMap.put("WeighmentSlip","RWeighmentSlip");
		relationMap.put("IBRCertificate","RIBRCertificate");
		relationMap.put("RoadPermit","RRoadPermit");
		relationMap.put("Otherdocuments2","ROtherdocuments2");
		relationMap.put("Otherdocuments3","ROtherdocuments3");
		relationMap.put("Otherdocuments4","ROtherdocuments4");
		relationMap.put("Otherdocuments5","ROtherdocuments5");
	
	
		Boolean aTrue=false;
		for(String key:relationMap.values()){
			Boolean val = (Boolean)currentRequest.getObject(key);
			if(val!=null){
				aTrue=aTrue||val;
			}
		}
	
		if(!aTrue)
		{
			throw new TBitsException("Please attach at least one Required document");
		}
	
	   //rule 2
		if(poRequest!=null && grnRequest!=null){
			String poPoNumber=poRequest.get(POnumber);
			String grnPoNumber=grnRequest.get(POnumber);
			if(poPoNumber!=null&&grnPoNumber!=null){				
				if(!poPoNumber.trim().equals(grnPoNumber.trim())){
					throw new TbitsExceptionClient("PO# does not matches in PO and GRN Records");
				}
			}
		}
	}


}


