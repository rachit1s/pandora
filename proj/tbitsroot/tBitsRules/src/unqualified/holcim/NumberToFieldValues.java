package holcim;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import transbit.tbits.domain.User;

public class NumberToFieldValues implements IRule{

	private static final String APPLICABLE_BAS_PROP = "holcim.documentnumber.to.metadata.bas";

	private static final String DOCUMENT_NUMBER = "VendorNo";
	
	private static final String PROJECT_CODE_FIELD = "DelCategory";
	private static final String DOCUMENT_CREATOR = "Originator";
	private static final String DOCUMENT_TYPE_MAJOR = "Discipline";
	private static final String DOCUMENT_TYPE_MINOR = "request_type_id";
	private static final String HOLCIM_ASSET_CODE_E = "Facility";
	private static final String HOLCIM_ASSET_CODE_F = "Asset";
	
	/**
	 * 0. Sanitize the number for spaces and special dashes (these are mistakes which user can always figure out)
	 * 1. If the document number changes, pick up various parts of it and update various fields
	 * Project Code - Document Creator - Document Type - Holcim Asset Code - Concecutive Number - Revision Index 
     *		G D 0 1 -       H G S      -     M E 0 1   -    2 1 6 R M 1    -       0 0 0 1      -      A 
	 * 2. Check the uniqueness
	 */
	@Override
	public RuleResult execute(Connection connection, BusinessArea ba,
			Request oldRequest, Request currentRequest, int Source, User user,
			boolean isAddRequest) {
		RuleResult rr = new RuleResult();
		if(!isApplicableBA(ba))
		{
			System.out.println("Skipping '" + this.getClass().getName() + "' rule because it is not applicable for ba '" + ba.getSystemPrefix() + "' ");
			rr.setCanContinue(true);
			return rr;
		}
		
		if(isAddRequest)
		{
			String documentNumber = (String) currentRequest.getObject(DOCUMENT_NUMBER);
//			documentNumber = cleanUpString(documentNumber);
			currentRequest.setObject(DOCUMENT_NUMBER, documentNumber);
			
			Hashtable<String, Integer> valuesToBeSet = new Hashtable<String, Integer>();
			valuesToBeSet.put(PROJECT_CODE_FIELD, 1);
			valuesToBeSet.put(DOCUMENT_CREATOR, 2);
			valuesToBeSet.put(DOCUMENT_TYPE_MAJOR, 4);
			valuesToBeSet.put(DOCUMENT_TYPE_MINOR, 3);
			valuesToBeSet.put(HOLCIM_ASSET_CODE_E, 5);
			valuesToBeSet.put(HOLCIM_ASSET_CODE_F, 6);
			
			String patternString = "([A-Z][A-Z][0-9][0-9])-([A-Z][A-Z][A-Z])-(([A-Z][A-Z])[0-9][0-9])-([A-Z0-9][A-Z0-9])[0-9]([A-Z0-9][A-Z0-9])[A-Z0-9]-([0-9][0-9][0-9][0-9])";
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(documentNumber);
			if(matcher.matches())
			{
				//Sets the values in currentRequest with the values captured by matched and field marked by valuesToBeSet
				rr = seds(currentRequest, matcher , valuesToBeSet, ba);
				if(!rr.canContinue())
					return rr;
			}
			else
			{
				rr.setMessage("The document number '" + documentNumber + "' is invalid. It has to be in the format: GD01-HGS-ME01-216RM1-0001");
				rr.setCanContinue(false);
				return rr;
			}
		}
		
		rr.setCanContinue(true);
		return rr;
	}

	private boolean isApplicableBA(BusinessArea ba) {
		boolean isApplicable = false;
		
		String applicableBAs = null;
		try
		{
			applicableBAs = PropertiesHandler.getAppProperties().getProperty(APPLICABLE_BAS_PROP);
		}catch(Throwable t)
		{
			t.printStackTrace();
		}
		if((applicableBAs != null) && (applicableBAs.length() > 0))
		{
			String [] appliBAs = applicableBAs.split(",");
			for(String sysPrefix: appliBAs)
			{
				if(sysPrefix.trim().toLowerCase().equals(ba.getSystemPrefix().toLowerCase()))
				{
					isApplicable = true;
					break;
				}
			}
		}
		return isApplicable;
	}

	/**
	 * Remove spaces, Translate to Upper case and normalize dashes
	 * @param documentNumber
	 * @return
	 */
	private String cleanUpString(String documentNumber) {
		return documentNumber.replaceAll("[ \\t\\n\\r]+","").toUpperCase()
		.replace("\u2010", "-").replace("\u2011", "-").replace("\u2012", "-").replace("\u2013", "-") // normalise hyphens
		.replace("\u2014", "-").replace("\u2015", "-").replace("\u002d", "-").replace("\u2212", "-");
	}

	private RuleResult seds(Request currentRequest, Matcher matcher, Hashtable<String, Integer> valuesToBeSet, BusinessArea ba) {
		RuleResult rr = new RuleResult();
		
		for(String fieldName:valuesToBeSet.keySet())
		{
			int captureIdx = valuesToBeSet.get(fieldName);
			String fieldValue = matcher.group(captureIdx);
			
			if(fieldValue == null)
			{
				rr.setMessage("The document number is invalid. It has to be in the format: GD01-HGS-ME01-216RM1-0001");
				System.err.println("The capture group at " + captureIdx + " is not found.");
				rr.setCanContinue(false);
				return rr;
			}
			Field field = null;
			try {
				field = Field.lookupBySystemIdAndFieldName(ba.getSystemId(), fieldName);
				
			} catch (DatabaseException e) {
				e.printStackTrace();
				rr.setCanContinue(false);
				rr.setMessage("Internal System error has occurred while connecting to database.");
				return rr;
			}
			if(field == null)
			{
				rr.setMessage("Unable to find field with the name '" + fieldName + "'");
				rr.setCanContinue(false);
				return rr;
			}
			try
			{
				
				switch(field.getDataTypeId())
				{
					case DataType.TYPE:
						try {
							Type type = Type.lookupBySystemIdAndFieldIdAndTypeName(ba.getSystemId(), field.getFieldId(), fieldValue);
							if(type == null)
							{
								rr.setMessage("Invalid value '" + fieldValue + "'  for '" + field.getDisplayName() + "' part of the number ");
								rr.setCanContinue(false);
								return rr;
							}
							currentRequest.setObject(field, type);
							
						} catch (DatabaseException e) {
							e.printStackTrace();
							rr.setCanContinue(false);
							rr.setMessage("Internal System error has occurred while connecting to database.");
							return rr;
						}
						break;
					
					case DataType.INT:
						currentRequest.setObject(field, Integer.parseInt(fieldValue));
						break;
					case DataType.BOOLEAN:
						currentRequest.setObject(field, Boolean.parseBoolean(fieldValue));
						break;
					case DataType.STRING:
						currentRequest.setObject(field, fieldValue);
						break;
					case DataType.REAL:
						currentRequest.setObject(field, Double.parseDouble(fieldValue));
						break;
					default:
						System.out.println("Skipped the part of the documentnumber. Invalid datatype for the document number parsing. BA: " + ba.getSystemPrefix() + ", field: " + field.getName());
						break;
				}
			}
			catch(Throwable t)
			{
				t.printStackTrace();
				rr.setMessage("Invalid value '" + fieldValue + "'  for '" + field.getDisplayName() + "' part of the number ");
				rr.setCanContinue(false);
				return rr;
			}
		}
		rr.setCanContinue(true);
		return rr;
	}

	/**
	 * Sets the valus of fields */
	@Override
	public double getSequence() {
		return 0;
	}

	@Override
	public String getName() {
		return this.getClass().getName();
	}

}
