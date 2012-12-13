package testCorr.rule;

import java.sql.Connection;
import java.util.Hashtable;

import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Type;
import transbit.tbits.exception.TBitsException;

public class CorrNumberConstraint implements ICorrConstraintPlugin {

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.constraints");
	
	public String getName() {
		return "This rule generates / validates the correspondence number.";
	}

	public double getOrder() {
		return 0.5;
	}

	public void execute1(Hashtable<String, Object> params) throws TBitsException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		Connection connection = (Connection) params.get(CONNECTION);
		
		Type disValue = coob.getDisableProtocol();
		if( null != disValue && disValue.getName().equalsIgnoreCase(GenericParams.DisableProtocol_True))
		{
			return;
		}
		
		BusinessArea ba = coob.getBa();
		
		FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
		if( null == corrNofne || null == corrNofne.getBaFieldName() )
		{
			LOG.info("Corr. No. or Corr. File was not configured hence not generating no. and file.");
			return;
		}
		
		if( coob.getSource() == CorrObject.SourcePreview )
		{
			// get the corr no. and set it.
			String corrNo = "TODO_correspondence_number_here";
			coob.getTtrd().set(corrNofne.getBaFieldName(), corrNo);
		}
		else
		{
			String corrNo = "TODO_correspondence_number_here";
			coob.getRequest().setObject(corrNofne.getBaFieldName(), corrNo);
		}
	}
	
	public void execute(Hashtable<String, Object> params) throws TBitsException 
	{
		CorrObject coob = (CorrObject) params.get(CORROBJECT);
		Connection connection = (Connection) params.get(CONNECTION);

		Type generate = coob.getGenerate();
		if( null == generate || generate.getName().equals(GenerateCorr_NoPdforCorrNumber))
			return ;
		
		BusinessArea ba = coob.getBa();
		
		FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
		if( null == corrNofne || null == corrNofne.getBaFieldName() )
		{
			LOG.info("Corr. No. was not configured hence not generating no.");
			return;
		}
		
		if( coob.getSource() == CorrObject.SourcePreview )
		{
			// get the corr no. and set it.
			String corrNo = "[Likely]corr_number_for_preview";
			coob.getTtrd().set(corrNofne.getBaFieldName(), corrNo);
		}
		else
		{
			String corrNo = "corr_number";
			coob.getRequest().setObject(corrNofne.getBaFieldName(), corrNo);
		}
			
	}


}
