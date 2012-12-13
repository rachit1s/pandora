package transmittal.com.tbitsGlobal.client.extensions;

import java.util.ArrayList;
import java.util.List;

import transmittal.com.tbitsGlobal.client.admin.TrnAdminConstants;
import transmittal.com.tbitsGlobal.client.admin.pages.AttachmentListView;
import transmittal.com.tbitsGlobal.client.admin.pages.CreateProcessView;
import transmittal.com.tbitsGlobal.client.admin.pages.DistListView;
import transmittal.com.tbitsGlobal.client.admin.pages.DrawingNumberFieldView;
import transmittal.com.tbitsGlobal.client.admin.pages.DropdownView;
import transmittal.com.tbitsGlobal.client.admin.pages.PostTrnFieldMapView;
import transmittal.com.tbitsGlobal.client.admin.pages.ProcessParamsView;
import transmittal.com.tbitsGlobal.client.admin.pages.ProcessesView;
import transmittal.com.tbitsGlobal.client.admin.pages.ReplicateProcessView;
import transmittal.com.tbitsGlobal.client.admin.pages.SrcTargetFieldMapView;
import transmittal.com.tbitsGlobal.client.admin.pages.ValidationRulesView;

import com.tbitsGlobal.admin.client.widgets.APPageLink;
import com.tbitsGlobal.admin.client.widgets.APTabItem;
import commons.com.tbitsGlobal.utils.client.Events.BaseTbitsObservable;
import commons.com.tbitsGlobal.utils.client.Events.TbitsObservable;

public class AdmLinks {
	protected static AdmLinks self;
	
	protected TbitsObservable observable;
	
	private static List<APPageLink> trnLinkList = new ArrayList<APPageLink>();
	
	private AdmLinks(){
		observable = new BaseTbitsObservable();
		observable.attach();
	
		initTrnProcessesMap();
		
		initDropdownMap();
		
		initSrcTargetFieldMap();
		
		initTrnProcessParamsMap();
		
		initPostTrnProcessMap();
		
		initAttachmentListMap();
		
		initDistListMap();
		
		initDrawingNumberMap();
		
		initValidationRulesTable();
		
		initReplicateProcessTab();
		
		initTrnCreateProcess();
	}
	
	/**
	 * Initialize Replicate Process Link
	 */
	protected void initReplicateProcessTab(){
		APPageLink replicateProcessLink = new APPageLink(TrnAdminConstants.REPLICATE_PROCESS){
			public APTabItem getPage() {
				return new ReplicateProcessView(linkIdentifier);
			}
		};
		trnLinkList.add(replicateProcessLink);
	}
	
	/**
	 * Initialize Validation Rules Link
	 */
	protected void initValidationRulesTable(){
		APPageLink validationRulesLink = new APPageLink(TrnAdminConstants.VALIDATION_RULES){
			public APTabItem getPage() {
				return new ValidationRulesView(linkIdentifier);
			}
		};
		trnLinkList.add(validationRulesLink);
	}
	
	/**
	 * Initialize Drawing Number Field link
	 */
	protected void initDrawingNumberMap(){
		APPageLink drawingNumberLink = new APPageLink(TrnAdminConstants.DRAWING_NUMBER_FIELD){
			public APTabItem getPage() {
				return new DrawingNumberFieldView(linkIdentifier);
			}
		};
		trnLinkList.add(drawingNumberLink);
	}
	
	/**
	 * Initialize Dropdown link
	 */
	protected void initDropdownMap(){
		APPageLink dropdownLink = new APPageLink(TrnAdminConstants.DROPDOWN_LIST){
			public APTabItem getPage() {
				return new DropdownView(linkIdentifier);
			}
		};
		trnLinkList.add(dropdownLink);
	}
	
	/**
	 * Initialize Attachment List Map link
	 */
	protected void initAttachmentListMap(){
		APPageLink attachmentListMapLink = new APPageLink(TrnAdminConstants.ATTACHMENT_LIST){
			public APTabItem getPage() {
				return new AttachmentListView(linkIdentifier);
			}
		};
		trnLinkList.add(attachmentListMapLink);
	}
	
	/**
	 * Initialize Distributin List Table link
	 */
	protected void initDistListMap(){
		APPageLink distListMapLink = new APPageLink(TrnAdminConstants.DIST_LIST){
			public APTabItem getPage() {
				return new DistListView(linkIdentifier);
			}
		};
		trnLinkList.add(distListMapLink);
	}
	
	/**
	 * Initialize Source Target Field Map link
	 */
	protected void initSrcTargetFieldMap(){
		APPageLink srcTargetFieldMapLink = new APPageLink(TrnAdminConstants.SRC_TARGET_FIELD_MAP){
			public APTabItem getPage() {
				return new SrcTargetFieldMapView(linkIdentifier);
			}
		};
		trnLinkList.add(srcTargetFieldMapLink);
	}
	
	/**
	 * Initialize Post Transmittal Process Map link
	 */
	protected void initPostTrnProcessMap(){
		APPageLink postTrnProcessMapLink = new APPageLink(TrnAdminConstants.POST_TRN_FIELD_MAP){
			public APTabItem getPage() {
				return new PostTrnFieldMapView(linkIdentifier);
			}
		};
		trnLinkList.add(postTrnProcessMapLink);
	}
	
	/**
	 * Initialize Transmittal Processes link
	 */
	protected void initTrnProcessesMap(){
		APPageLink trnProcessesMapLink = new APPageLink(TrnAdminConstants.TRN_PROCESSES){
			public APTabItem getPage() {
				return new ProcessesView(linkIdentifier);
			}
		};
		trnLinkList.add(trnProcessesMapLink);
	}
	
	/**
	 * Initialize Transmittal Process Parameters link
	 */
	protected void initTrnProcessParamsMap(){
		APPageLink trnProcessParamMapLink = new APPageLink(TrnAdminConstants.TRN_PROCESS_PARAMS){
			public APTabItem getPage() {
				return new ProcessParamsView(linkIdentifier);
			}
		};
		trnLinkList.add(trnProcessParamMapLink);
	}
	
	
	/**
	 * Initialize the Create new process link
	 */
	protected void initTrnCreateProcess(){
		APPageLink createTrnProcessLink = new APPageLink(TrnAdminConstants.CREATE_PROCESS){
			public APTabItem getPage() {
				return new CreateProcessView(linkIdentifier);
			}
		};
		trnLinkList.add(createTrnProcessLink);
	}
	
	public static AdmLinks getInstance(){
		if(self == null)
			self = new AdmLinks();
		return self;
	}
	
	public List<APPageLink> getPageList(){
		return trnLinkList;
	}
}
