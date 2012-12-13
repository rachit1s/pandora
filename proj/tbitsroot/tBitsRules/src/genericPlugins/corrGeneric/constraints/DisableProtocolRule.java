package corrGeneric.constraints;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.tdn;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_NoPdforCorrNumber;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class DisableProtocolRule implements ICorrConstraintPlugin {

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.rule");

	public String getName() {
		return "Diable protocol can be true only allowed with NoPdfAndNoNumber Generate Option";
	}

	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		CorrObject co = (CorrObject) params.get(CORROBJECT);
		FieldNameEntry genCorrFne = FieldNameManager.lookupFieldNameEntry(co.getBa().getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
		FieldNameEntry disableProtFne = FieldNameManager.lookupFieldNameEntry(co.getBa().getSystemPrefix(), GenericParams.DisableProtocolFieldName);
		
		if( null != co.getDisableProtocol() && null != co.getGenerate() )
		{
			Type disValue = co.getDisableProtocol();
			Type genValue = co.getGenerate();
			
			if( null != disValue && null != genValue )
			{
				if( disValue.getName().equalsIgnoreCase(GenericParams.DisableProtocol_True) && !genValue.getName().equalsIgnoreCase(GenerateCorr_NoPdforCorrNumber) )
				{
					throw new CorrException("When '" + fdn(co.getBa(), disableProtFne.getBaFieldName()) + "' is true then only value allowed in '" + fdn(co.getBa(), genCorrFne.getBaFieldName()) + "' is '" + tdn(co.getBa(), genCorrFne.getBaFieldName(), GenerateCorr_NoPdforCorrNumber) + "'");							
				}
			}
		}
	}

	public double getOrder() {
		return 0.3;
	}

}
