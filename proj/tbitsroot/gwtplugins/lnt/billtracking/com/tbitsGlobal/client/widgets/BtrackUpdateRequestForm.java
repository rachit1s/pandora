package billtracking.com.tbitsGlobal.client.widgets;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import billtracking.com.tbitsGlobal.client.BillCache;
import billtracking.com.tbitsGlobal.server.BillProperties;
import billtracking.com.tbitsGlobal.shared.IBillConstants;
import billtracking.com.tbitsGlobal.shared.IBillProperties;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.tbitsGlobal.jaguar.client.widgets.forms.UpdateRequestForm;
import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;
import commons.com.tbitsGlobal.utils.client.UIContext.UIContext;
import commons.com.tbitsGlobal.utils.client.bafield.BAField;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldCheckBox;
import commons.com.tbitsGlobal.utils.client.bafield.BAFieldString;
import commons.com.tbitsGlobal.utils.client.domainObjects.PermissionClient;

public class BtrackUpdateRequestForm extends UpdateRequestForm
implements IBillConstants,IBillProperties {

	private Button verifyDocsBtn;
	HashMap <String,BAField> bafieldMap;
	BillCache bCache;
	public BtrackUpdateRequestForm(UIContext parentContext) {
		super(parentContext);
		bafieldMap = new HashMap<String,BAField>();
		genBAFieldCheckBoxMap();
		verifyDocsBtn = new Button("Verify Documents", new SelectionListener<ButtonEvent>(){
			@Override
			public void componentSelected(ButtonEvent ce) {
				onClickVeriFyDocs();
			}

		});

		bCache = BillCache.getInstance();
		bCache.load();


		// TODO Auto-generated constructor stub
	}


	private void onClickVeriFyDocs() {
		// TODO Auto-generated method stub
		TbitsTreeRequestData model = this.createRequestModel();
		String attMsg = getMissingAttachmentsMessage(model);
		MessageBox mb = new MessageBox();
		if(attMsg!=null){
			if(attMsg.isEmpty()){
				mb.setTitle("Required Documents");
				mb.setMessage("All Required Documents Uploaded");
				mb.show();
			}else{
				//Window.confirm(attMsg);
				mb.setTitle("Required Documents");
				mb.setMessage(attMsg);
				mb.show();
			}
		}

		submitBtn.enable();
	}
	@Override
	protected boolean shouldFillField(BAField baField) {
		// TODO Auto-generated method stub
		return baField.isSetEnabled();
	}

	@Override
	protected boolean hasBAFieldPermission(BAField bafield) {

		return baFieldChangePermIncludingDynamicRoles(bafield);

	}

	@Override
	protected void afterRender() {
		super.afterRender();

		draftTimer = new Timer(){
			@Override
			public void run() {
				saveDraft();
			}};
			draftTimer.scheduleRepeating(30 * 1000);

			if(bCache.getIsVerifyer()){
				this.addButton(verifyDocsBtn);
				submitBtn.disable();
			}
			else submitBtn.enable();
	}
    /*
     * (non-Javadoc)
     * @see commons.com.tbitsGlobal.utils.client.widgets.forms.AbstractEditRequestForm#beforeSubmit(commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData)
       runs checks prior to request submission
     */
	@Override
	protected boolean beforeSubmit(TbitsTreeRequestData requestModel)
	{
		if(submitBtn != null)
			submitBtn.disable();
		if(bCache.getGrnCheck()){
			String grnsesno=requestModel.getAsString(GRN_);
			String relatedRequests=requestModel.getAsString(RELATED_REQUESTS);
			String grnPrefix=bCache.getBillProperties().get(PROPERTY_GRN_BA_PREFIX);
			if(relatedRequests.contains(grnPrefix))
				return true;
			if (grnsesno==null||grnsesno.isEmpty()) {
				Boolean cont = Window
				.confirm("GRN/SES/DPR# is Empty do you wish to continue");
				return cont;
			}
		}
			return true;
	}

	private boolean baFieldChangePermIncludingDynamicRoles(BAField bafield) {
		int perm = 0;
		TbitsTreeRequestData requestModel = this.getData().getRequestModel();
		if(requestModel != null && requestModel.getPerms().containsKey(bafield.getName()))
			perm = requestModel.getPerms().get(bafield.getName());

		return ((perm & PermissionClient.CHANGE) != 0) && bafield.isCanUpdate() ;
	}


	void genBAFieldCheckBoxMap(){
		List<BAField> bafieldList=this.getData().getBAFields().getModels();
		Iterator<BAField> i=bafieldList.iterator();
		while(i.hasNext()){
			BAField baField=i.next();
			if((baField instanceof BAFieldCheckBox|| baField instanceof BAFieldString) && baField.isCanUpdate()){
				String name=baField.getName();
				bafieldMap.put(name,baField);
			}
		}


	}
	public  String getMissingAttachmentsMessage(TbitsTreeRequestData model){
		genBAFieldCheckBoxMap();
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
		relationMap.put("eALPSprintout","ReALPSprintout");
		relationMap.put("WeighmentSlip","RWeighmentSlip");
		relationMap.put("IBRCertificate","RIBRCertificate");
		relationMap.put("RoadPermit","RRoadPermit");


		StringBuilder sb = new StringBuilder();
		for(String req:relationMap.keySet()){
			Boolean reqValue=model.get(req);
			if(reqValue!=null && reqValue==true){
				String rec=relationMap.get(req);
				Boolean recValue=model.get(rec);
				if(recValue!=null && recValue==false){
					BAField field=bafieldMap.get(rec);
					sb.append(field.getDisplayName()+"<br>");
				}
			}


		}

		relationMap.clear();

		relationMap.put("Otherdocuments","ROtherdocuments");
		relationMap.put("Otherdocuments2","ROtherdocuments2");
		relationMap.put("Otherdocuments3","ROtherdocuments3");
		relationMap.put("Otherdocuments4","ROtherdocuments4");
		relationMap.put("Otherdocuments5","ROtherdocuments5");

		for(String req:relationMap.keySet()){
			String reqValue=model.get(req);
			if(reqValue!=null && !reqValue.isEmpty()){
				String rec=relationMap.get(req);
				Boolean recValue=model.get(rec);
				if(recValue!=null && recValue==false){
					BAField field=bafieldMap.get(rec);
					sb.append(field.getDisplayName()+"<br>");
				}
			}


		}
		return sb.toString();
	}
}

