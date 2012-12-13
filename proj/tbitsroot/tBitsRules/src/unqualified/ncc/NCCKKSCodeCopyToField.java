package ncc;
import java.sql.Connection;
import java.util.Arrays;
import java.util.List;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

/**
 * 
 */

/**
 * @author Lokesh
 *
 */
public class NCCKKSCodeCopyToField implements IRule {

	private static final String PLUGINS_NCC_NCCKKSCODE_COPY_TO_FIELD_BA_LIST = "plugins.ncc.NCCKKSCodeCopyToField.baList";
	private static final String FIELD_NAME_KKS_CODE = "KKSCode";

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#execute(java.sql.Connection, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.Request, transbit.tbits.domain.Request, int, transbit.tbits.domain.User, boolean)
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		
		RuleResult ruleResult = new RuleResult();
		String kksCode = null;
		String baListStr = PropertiesHandler.getProperty(PLUGINS_NCC_NCCKKSCODE_COPY_TO_FIELD_BA_LIST);
		if (baListStr != null){
			List<String> baList = Arrays.asList(baListStr.trim().split(","));
			if ((baList != null) && (!baList.contains(ba.getSystemPrefix())))
				return ruleResult;
		}
		
		Type systemCode = null, equipmentCode = null, componentCode = null;
		try {
			systemCode = (Type)currentRequest.getObject(NCCTransmittalUtils.SYSTEM_CODE);
			equipmentCode = (Type)currentRequest.getObject(NCCTransmittalUtils.EQUIPMENT_CODE);
			componentCode = (Type)currentRequest.getObject(NCCTransmittalUtils.COMPONENT_CODE);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} 

		if ((componentCode != null) && (!componentCode.getName().equals("None"))){
			kksCode = componentCode.getName();
		}
		else if ((equipmentCode != null) && (!equipmentCode.getName().equals("None"))){
			kksCode = equipmentCode.getName();
		}
		else if ((systemCode != null) && (!systemCode.getName().equals("None"))){
			kksCode = systemCode.getName();
		}
		else{
			ruleResult.setMessage("All the fields in KKS Numbering System  group were not " +
			"set appropriate value or set to 'None', hence did not generate set KKS Code field.");
			ruleResult.setCanContinue(true);
			return ruleResult;
		}
		
		if (kksCode != null)
			currentRequest.setObject(FIELD_NAME_KKS_CODE, kksCode);
		
		return ruleResult;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getName()
	 */
	@Override
	public String getName() {
		return this.getClass().getSimpleName() + ": Copies the KKS Code from one of the fields from which the KKS Code has to be considered";
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.api.IRule#getSequence()
	 */
	@Override
	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}

}
