package corrGeneric.rule;

import static corrGeneric.com.tbitsGlobal.server.util.Utility.fdn;
import static corrGeneric.com.tbitsGlobal.server.util.Utility.tdn;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_BothNumberAndPdf;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_NoPdforCorrNumber;
import static corrGeneric.com.tbitsGlobal.shared.objects.GenericParams.GenerateCorr_OnlyPdfWithSpecifiedNumber;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Uploader;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import corrGeneric.com.tbitsGlobal.server.interfaces.ICorrConstraintPlugin;
import corrGeneric.com.tbitsGlobal.server.managers.CorrPluginManager;
import corrGeneric.com.tbitsGlobal.server.managers.FieldNameManager;
import corrGeneric.com.tbitsGlobal.server.protocol.CorrObject;
import corrGeneric.com.tbitsGlobal.server.util.GenPDFUtil;
import corrGeneric.com.tbitsGlobal.server.util.Utility;
import corrGeneric.com.tbitsGlobal.shared.domain.FieldNameEntry;
import corrGeneric.com.tbitsGlobal.shared.objects.GenericParams;

public class CorrAddRequestRule implements IRule 
{
	public static TBitsLogger LOG = TBitsLogger.getLogger("corrGeneric.constraints");
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) 
	{
		if( Source == TBitsConstants.SOURCE_EMAIL )
		{
			return new RuleResult(true,"Rule not applicable for adding request from Email.", true);
		}

		String appBas = null;
		try
		{
			appBas = PropertiesHandler.getProperty(GenericParams.CommaSeparatedListOfApplicableBa);
		}
		catch(Exception iae)
		{
			LOG.error(TBitsLogger.getStackTrace(iae));
		}
		
		if( null == appBas || appBas.trim().equals("") )
			return new RuleResult(true, "No BA configured for Correspondence.", true );
		
		
		ArrayList<String> sysPrefixes = Utility.splitToArrayList(appBas.trim());
		
		if( null == ba || null == ba.getSystemPrefix() || ! sysPrefixes.contains(ba.getSystemPrefix()))
			return new RuleResult(true,"Either the ba was null Or was not applicable for correspondence module.", true);
		
		try 
		{
			FieldNameEntry genCorrFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.GenerateCorrespondenceFieldName);
			
			if( genCorrFne == null || genCorrFne.getBaFieldName() == null )
			{
				LOG.info("No Mapping found for " + GenericParams.GenerateCorrespondenceFieldName + " in the ba : " + ba.getSystemPrefix() + ". Hence not considering it as a part of corr. protocol.");
				return new RuleResult(true,"Rule executed successfully for request : " + currentRequest, true);			
			}
			else
			{
				String value = currentRequest.get(genCorrFne.getBaFieldName());
								
				if ( value != null )
				{
					CorrObject coob = new CorrObject(currentRequest, oldRequest);
					LOG.info("Coob : " + coob);

					Hashtable<String,Object> params = new Hashtable<String,Object>();
					params.put(ICorrConstraintPlugin.CONNECTION, connection);
					params.put(ICorrConstraintPlugin.CORROBJECT, coob);
					
					CorrPluginManager.getInstance().executeConstraints(params);
					
					if( value.equals(GenerateCorr_BothNumberAndPdf) || value.equals(GenerateCorr_OnlyPdfWithSpecifiedNumber))
					{
						FieldNameEntry corrFileFne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceFileFieldName);
						if( null == corrFileFne || corrFileFne.getBaFieldName() == null )
							return new RuleResult(true,"The " + GenericParams.CorrespondenceFileFieldName + " was not configured. Hence Not generating file.");
						
						File f = GenPDFUtil.generateAndGetFile(connection, coob, null);
						if( f != null )
						{
							Uploader uploader = new Uploader();
							AttachmentInfo info = uploader.copyIntoRepository(f);
							if( null == info )
								throw new TBitsException("Uploader failed to upload the generated correspondence file.");
							ArrayList<AttachmentInfo> files = new ArrayList<AttachmentInfo>();
							
							FieldNameEntry corrNofne = FieldNameManager.lookupFieldNameEntry(ba.getSystemPrefix(), GenericParams.CorrespondenceNumberFieldName);
							if( null != corrNofne && null != corrNofne.getBaFieldName() )
							{
								String corrNo = currentRequest.get(corrNofne.getBaFieldName());
								if(null != corrNo )
									info.setName(corrNo+".pdf");
							}
							
							files.add(info);
							currentRequest.setObject(corrFileFne.getBaFieldName(), files);
						}
						else
						{
							throw new TBitsException("Generation of correspondence file failed.");
						}
					}
				}
			}
		} catch (TBitsException e) {
			e.printStackTrace();
			return new RuleResult(false,e.getDescription(), false);
		}
		 catch (Exception e) {
				e.printStackTrace();
				return new RuleResult(false,e.getMessage(), false);
			}
		
		return new RuleResult(true,"Rule executed successfully for request : " + currentRequest, true);
	}

	public String getName() {
		return "Add Request rule for correspondence.";
	}

	public double getSequence() {
		return 5;
	}

}
