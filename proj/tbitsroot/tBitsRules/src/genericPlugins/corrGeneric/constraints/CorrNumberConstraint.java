package corrGeneric.constraints;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Type;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrException;
import corrGeneric.com.tbitsGlobal.shared.objects.CorrNumberConfigNotFoundException;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;
import corrGeneric.others.GenCorrNoHelper;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.*;

public class CorrNumberConstraint implements ICorrConstraintPlugin {

	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.constraints");
	
	public void execute(Hashtable<String, Object> params) throws CorrException 
	{
		try
		{
			CorrObject coob = (CorrObject) params.get(CORROBJECT);
			Connection connection = (Connection) params.get(CONNECTION);
			
			Type generate = coob.getGenerate();
			if( null == generate || generate.getName().equals(GenerateCorr_NoPdforCorrNumber))
				return ;
			
			BusinessArea ba = coob.getBa();
			
			// this will throw exception if corr. no. config. is not configured
			// the exception is handled and ignored.
			GenCorrNoHelper.getCorrNumberEntry(coob);
			
			FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
			if( null == corrNofne || null == corrNofne.getBaFieldName() )
			{
				LOG.info("Corr. No. was not configured hence not generating no.");
				return;
			}
			
			if( coob.getSource() == CorrObject.SourcePreview )
			{
				
					// get the corr no. and set it.
					String corrNo = GenCorrNoHelper.getCorrNo(coob, connection);
					coob.getTtrd().set(corrNofne.getBaFieldName(), corrNo);
			}
			else
			{
				// "kdi_corr,RJP_Corr,JPN_Corr,APPDCL_Corr,Malwa_Corr,VPGL2_Corr,IOM";
				String appBas = PropertiesHandler.getProperty(GenericParams.CorrBaList);
				if( null == appBas )
				{
					LOG.info("Property not found : " + GenericParams.CorrBaList + " in tbits_properties.");
					return ;
				}
				
				ArrayList<String> baList = Utility.splitToArrayList(appBas, ",");
				
				if(!baList.contains(ba.getSystemPrefix()))
				{
					LOG.info("CorrNumberGeneration not valid for this ba : " + ba.getSystemPrefix());
					return ; // we don't want to generate corr. no. in DI
				}
				
					String corrNo = GenCorrNoHelper.getCorrNo(coob, connection);
					coob.getRequest().setObject(corrNofne.getBaFieldName(), corrNo);
			}
		
		}
		catch( CorrNumberConfigNotFoundException e)
		{
			e.printStackTrace();
			Utility.LOG.info("Ignoring number generation from generic constraint. Client specific constraint might handle it.");
			return;
		}
			
	}

	public String getName() {
		return "This rule generates / validates the correspondence number.";
	}

	public double getOrder() {
		return 0.4;
	}

}
