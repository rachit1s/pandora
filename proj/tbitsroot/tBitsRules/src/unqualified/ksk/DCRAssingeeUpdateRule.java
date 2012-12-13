/**
 * 
 */
package ksk;

import java.sql.Connection;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AddRequest;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.APIException;

/**
 * @author lokesh
 *
 */
public class DCRAssingeeUpdateRule {

	private static final String DEPT_GENERAL = "General";

	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger("ksk");
	
	private static final String DEPT_CIVIL = "Civil";
	private static final String DEPT_CONTROL_INSTRUMENTATION = "ControlInstrumentation";
	private static final String DEPT_ELECTRICAL = "Electrical";	
	private static final String DEPT_GENERAL_LAYOUT = "GeneralLayout";
	private static final String DEPT_MECHANICAL = "Mechanical";	
	
	private static final String CEC_ELECTRICAL_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.cecElectricalAssignees");
	private static final String CEC_CNI_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.cecCnIAssignees");
	private static final String CEC_CIVIL_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.cecCivilAssignees");
	private static final String CEC_MECHANICAL_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.cecMechanicalAssignees");
		
	private static final String PHO_ELECTRICAL_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoElectricalAssignees");
	private static final String PHO_CNI_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoCnIAssignees");
	private static final String PHO_CIVIL_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoCivilAssignees");
	private static final String PHO_GENERAL_LAYOUT_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoGeneralLayoutAssignees");
	private static final String PHO_MECHANICAL_ASSIGNEES =  KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoMechanicalAssignees");
	private static final String PHO_ASH_HANDLING_ASSIGNEES =  KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoAshHandlingAssignees");
	private static final String PHO_PROJECT_ASSIGNEES =  KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoProjectAssignees");
	private static final String PHO_QA_ASSIGNEES =  KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoQAAssignees");
	private static final String PHO_SITE_ASSIGNEES =  KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.phoSiteAssignees");

	private static final String ELECON_CEC_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.EleconCECAssignees");
	private static final String ELECON_PHO_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.EleconPHOAssignee");
	private static final String ELECON_DCPL_ASSIGNEES = KSKUtils.getProperty("ksk.irule.DCRAssigneeUpdateRule.EleconDcplAssignees");
			
	public static String getAssigneeList(Connection connection, BusinessArea ba,
			Request srcDCRRequest, String transmittalType, boolean isAddRequest) {

		//String applicableBAList = KSKUtils.getProperty(KSK_IRULE_DCR_ASSIGNEE_UPDATE_RULE);
		//boolean isApplicableBA = KSKUtils.isExistsInString(applicableBAList , ba.getSystemPrefix());
		if (isAddRequest){
			Type deptType = srcDCRRequest.getRequestTypeId();
			String department = deptType.getName();

			if (KSKUtils.isCECDCR(ba)){
				if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){
					if (department.equals(DEPT_ELECTRICAL)){
						return CEC_ELECTRICAL_ASSIGNEES;
					}
					else if (department.equals(DEPT_CONTROL_INSTRUMENTATION)){
						return CEC_CNI_ASSIGNEES;
					}
					else if (department.equals(DEPT_CIVIL)){
						return CEC_CIVIL_ASSIGNEES;
					}else {
						return CEC_MECHANICAL_ASSIGNEES;
					}
				}
				else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO)){
					return ELECON_CEC_ASSIGNEES;
				}
			}
			else if (KSKUtils.isPHODCR(ba)){
				if (transmittalType.equals(KSKUtils.TRANSMIT_TO_WPCL)){
					if (department.equals(DEPT_ELECTRICAL)){
						return PHO_ELECTRICAL_ASSIGNEES;
					}
					else if (department.equals(DEPT_CONTROL_INSTRUMENTATION)){
						return PHO_CNI_ASSIGNEES;
					}
					else if (department.equals(DEPT_CIVIL)){
						return PHO_CIVIL_ASSIGNEES;
					}
					else if (department.equals(DEPT_MECHANICAL)){
						return PHO_MECHANICAL_ASSIGNEES;
					}
					else if (department.equals(DEPT_GENERAL_LAYOUT) || department.equals(DEPT_GENERAL)){
						return PHO_GENERAL_LAYOUT_ASSIGNEES;
					}
					else if (department.equals("HVAC") || department.equals("Project")){					
						return PHO_PROJECT_ASSIGNEES;
					}
					else if (department.equals("SiteErectionCommissioning")){					
						return PHO_SITE_ASSIGNEES;
					}
					else if (department.equals("QAQC")){					
						return PHO_QA_ASSIGNEES;
					}
					else {
						return PHO_ASH_HANDLING_ASSIGNEES;
					}
				}
				else if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO)){
					return ELECON_PHO_ASSIGNEES;
				}
			}
			else if (KSKUtils.isDCPLDCR(ba)){
				if (transmittalType.equals(KSKUtils.TRANSMIT_TO_ELECON_PHO))
					return ELECON_DCPL_ASSIGNEES;
			}
		}
		return "";
	}
	
	public static void main(String[] args) throws APIException{		
		AddRequest addRequest = new AddRequest();
		addRequest.setSource(TBitsConstants.SOURCE_CMDLINE);
		Hashtable<String, String> paramTable = new Hashtable<String, String>();
		paramTable.put(Field.LOGGER, "root") ;
		paramTable.put(Field.ASSIGNEE, "venugopalrao.a") ;
		paramTable.put(Field.SUBJECT, "test assignee1") ;
		paramTable.put(Field.DESCRIPTION, "test assignee1") ;
		paramTable.put(Field.USER, "root") ;
	    paramTable.put(Field.BUSINESS_AREA, "16") ;
	    paramTable.put(Field.REQUEST_TYPE, "Electrical");
		addRequest.addRequest(paramTable);
	}
	
}
