/**
 * 
 */
package ncc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import transbit.tbits.ExtUI.IAddRequestFooterSlotFiller;
import transbit.tbits.ExtUI.IUpdateRequestFooterSlotFiller;
import transbit.tbits.common.DTagReplacer;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.PropertiesHandler;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import transbit.tbits.domain.Type;

/**
 * @author lokesh
 *
 */
public class NCCKKSCodePrefillRule implements IAddRequestFooterSlotFiller, IUpdateRequestFooterSlotFiller{

	private static final String TRANSBIT_TBITS_TRANSMITTAL_KKS_CODE_PREFILL_BA_LIST =
										"transbit.tbits.transmittal.ncc.kksCodePrefillBAList";
	private static final String SRC_FIELD_ID = "src_field_id";
	private static final String SYS_ID = "sys_id";
	private static final String NCC_KKS_MAPPING = "ncc_kks_mapping";
	public static final TBitsLogger LOG = TBitsLogger.getLogger("ncc");
	private static final String KKS_CODE_SELECTION = "kks_code_selection.js";
	private static final String MSG_CANNOT_PREFILL =  "Cannot pre-fill form. Please select workflow manually.";;
	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IAddRequestFooterSlotFiller#getAddRequestFooterHtml(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, transbit.tbits.domain.BusinessArea, transbit.tbits.domain.User)
	 */
	
	public String getAddRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba, User user) {
		return getPrefillHtml(ba);
	}

	/**
	 * @param ba
	 * @return
	 */
	private String getPrefillHtml(BusinessArea ba) {
		
		String baList = PropertiesHandler.getProperty(TRANSBIT_TBITS_TRANSMITTAL_KKS_CODE_PREFILL_BA_LIST);
		if((baList == null) || (!TransmittalUtils.isExistsInString(baList, ba.getSystemPrefix())))
			return "";
		
		int systemId = ba.getSystemId();
		Field systemCodeField = null, equipmentCodeField = null, componentCodeField = null;
		ArrayList<Type> systemCodeTypes = null, eqpCodeTypes = null, componentCodeTypes = null;
		try {
			systemCodeField = Field.lookupBySystemIdAndFieldName(systemId, TransmittalUtils.SYSTEM_CODE);
		    systemCodeTypes = Type.lookupAllBySystemIdAndFieldName(systemId, TransmittalUtils.SYSTEM_CODE);
			equipmentCodeField = Field.lookupBySystemIdAndFieldName(systemId, TransmittalUtils.EQUIPMENT_CODE);
			eqpCodeTypes = Type.lookupAllBySystemIdAndFieldName(systemId, TransmittalUtils.EQUIPMENT_CODE);
			componentCodeField = Field.lookupBySystemIdAndFieldName(systemId, TransmittalUtils.COMPONENT_CODE);
			componentCodeTypes = Type.lookupAllBySystemIdAndFieldName(systemId, TransmittalUtils.COMPONENT_CODE);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
			
				
		if ((systemCodeField == null) || (equipmentCodeField == null) || (componentCodeField == null)) {
			return "";
		}
		ArrayList<KKSCodeMapObject> systemEquipMapList = getKSKCodeMapList(systemId, systemCodeField);		
		String systemEquipmentTypesMapJson = getJsonArrayOfSourceTargetTypesMap(systemCodeTypes, eqpCodeTypes,
				systemEquipMapList);
		
		ArrayList<KKSCodeMapObject> equipmentComponentMapList = getKSKCodeMapList(systemId, equipmentCodeField);		
		String equipmentComponentTypesMapJson = getJsonArrayOfSourceTargetTypesMap(eqpCodeTypes, componentCodeTypes,
				equipmentComponentMapList);
		
		/*if(!ba.getSystemPrefix().equals("KNPL_NCC"))
			return "";*/
		URL fileURL = getClass().getResource(KKS_CODE_SELECTION) ;
		if( null == fileURL )
		{
			LOG.error( "File not found = " + KKS_CODE_SELECTION ) ;
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		}
		
		String filePath = fileURL.getFile() ;
		if( filePath.equals(""))
		{
			LOG.error( "File not found = " + KKS_CODE_SELECTION ) ;
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		}		
	
//		 test if DtagReplacer can find my file 
		DTagReplacer dtagreplacer = null ;		
		File myFile = new File(filePath) ;
		try {
			 dtagreplacer = new DTagReplacer( myFile ) ;
			 if ((systemEquipmentTypesMapJson == null) || (equipmentComponentTypesMapJson == null)){
				 return TransmittalUtils.showError(MSG_CANNOT_PREFILL);
			 }
			 else{
				 String eqpTypesJson = getKKSCodTypesJson(eqpCodeTypes);
				 if (eqpTypesJson != null)
					 dtagreplacer.replace("equipmentCodeTypes", eqpTypesJson);
				 else
					 return TransmittalUtils.showError(MSG_CANNOT_PREFILL);
				 
				 String compTypesJson = getKKSCodTypesJson(componentCodeTypes);
				 if (compTypesJson != null)
					 dtagreplacer.replace("componentCodeTypes", compTypesJson);
				 else
					 return TransmittalUtils.showError(MSG_CANNOT_PREFILL);			
				 
				 
				 dtagreplacer.replace("equipmentCodeMap", systemEquipmentTypesMapJson);
				 dtagreplacer.replace("componentCodeMap", equipmentComponentTypesMapJson);
			 }
		} catch (FileNotFoundException e1) {
			LOG.error("DTagReplacer Exception : file not found" ) ;
			e1.printStackTrace();
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		} catch (IOException e1) {
			LOG.error("DTagReplacer Exception" ) ;
			e1.printStackTrace();
			String sher = TransmittalUtils.showError(MSG_CANNOT_PREFILL) ;
//			tagTable.put(KskConstants.EXT_SUBMIT_BUTTON_LIST, sher ) ;
			return sher;
		}

		return dtagreplacer.parse();
	}

	/**
	 * @param kksCodeTypes
	 * @return
	 */
	private String getKKSCodTypesJson(ArrayList<Type> kksCodeTypes) {
		ArrayList<KKSType> eqpKksTypeList = getKKSTypeList(kksCodeTypes);
		 Gson gson = new Gson();
		 String eqpTypesJson = gson.toJson(eqpKksTypeList, new TypeToken<ArrayList<KKSType>>(){}.getType());
		return eqpTypesJson;
	}
	
	ArrayList<KKSType> getKKSTypeList(ArrayList<Type> typeList){
		ArrayList<KKSType> kksTypeList = new ArrayList<KKSType>();
		for (Type type : typeList){
			if(type != null){
				KKSType kksType = new KKSType(type.getName(), type.getDisplayName());
				kksTypeList.add(kksType);
			}				
		}
		return kksTypeList;
	}

	/**
	 * @param sourceCodeTypes
	 * @param targetCodeTypes
	 * @param sourceTargetMapList
	 * @param srcTargetTypesMap
	 */
	private String getJsonArrayOfSourceTargetTypesMap(
			ArrayList<Type> sourceCodeTypes, ArrayList<Type> targetCodeTypes,
			ArrayList<KKSCodeMapObject> sourceTargetMapList) {
		
		HashMap<String, ArrayList<KKSType>> srcTargetTypesMap = new HashMap<String, ArrayList<KKSType>>();
		if ((sourceCodeTypes != null) && (sourceTargetMapList != null))

			//Loop for each source type.
			for(Type srcType : sourceCodeTypes){
				if (srcType != null){
					//Loop to accumulate the target types for each source type. 
					ArrayList<KKSType> targetTypesList = new ArrayList<KKSType>();//srcTargetTypesMap.get(srcType.getName());
					for(KKSCodeMapObject kksCodeMapObject  : sourceTargetMapList){
						if ((srcType.getFieldId() ==  kksCodeMapObject.srcFieldId) 
								&& srcType.getTypeId() == kksCodeMapObject.srcTypeId){	
							KKSType kksType = getTypeName(kksCodeMapObject.targetFieldId, kksCodeMapObject.targetTypeId,
									targetCodeTypes);
							if (kksType != null){
								targetTypesList.add(kksType);									
							}
						}
					}
					srcTargetTypesMap.put(srcType.getName(), targetTypesList);
				}
			}

		HashMap <String, String> jsonTypesMap = new HashMap<String, String>();
		Gson gson = new Gson();
		for(String srcTypeName: srcTargetTypesMap.keySet()){
			java.lang.reflect.Type collectionType = new TypeToken<ArrayList<KKSType>>(){}.getType();
			String targetTypesJsonString = gson.toJson(srcTargetTypesMap.get(srcTypeName), collectionType);
			jsonTypesMap.put(srcTypeName, targetTypesJsonString);			
		}
		return gson.toJson(jsonTypesMap, new TypeToken<HashMap<String,String>>(){}.getType());
	}

	private KKSType getTypeName(int fieldId, int typeId, ArrayList<Type> typeList){
		for(Type type : typeList)
			if (type != null)
				if ((fieldId == type.getFieldId()) && (typeId == type.getTypeId()))
					return new KKSType(type.getName(), type.getDisplayName());
		return null;
	}
	

	/**
	 * @param ba
	 * @param srcField
	 * @param equipmentCodeField
	 * @param componentCodeField
	 */
	private ArrayList<KKSCodeMapObject> getKSKCodeMapList(int systemId, Field srcField) {
		Connection connection = null;
		ArrayList<KKSCodeMapObject> kksCodeList = new ArrayList<KKSCodeMapObject>();
		try{			
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM " + NCC_KKS_MAPPING + " WHERE " + SYS_ID + "=? and "
					+ SRC_FIELD_ID + "=?");
			ps.setInt(1, systemId);
			ps.setInt(2, srcField.getFieldId());
			ResultSet rs = ps
			.executeQuery();
			if(rs != null){				
				while(rs.next()){
					KKSCodeMapObject kksCodeMap = new KKSCodeMapObject(rs.getInt(1), rs.getInt(2), 
							rs.getInt(3), rs.getInt(4), rs.getInt(5));
					kksCodeList.add(kksCodeMap);
				}
			}
			rs.close();
			ps.close();		

		}catch(SQLException e){
			e.printStackTrace();
		}finally{
			try {
				if((connection != null) && (!connection.isClosed()))			
					connection.close();
			} catch (SQLException e) {
				LOG.error(new Exception("Unable to close the connection to the database.", e));
			}
		}
		return kksCodeList;
	}	
	

	/* (non-Javadoc)
	 * @see transbit.tbits.ExtUI.IAddRequestFooterSlotFiller#getAddRequestFooterSlotFillerOrder()
	 */
	
	public double getAddRequestFooterSlotFillerOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	
	public String getUpdateRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request oldRequest, User user) {
		return getPrefillHtml(ba);
	}

	
	public double getUpdateRequestFooterSlotFillerOrder() {
		// TODO Auto-generated method stub
		return 0;
	}
}

final class KKSType{
	String name;
	String displayName;
	public KKSType(String name, String displayName){
		this.name = name;
		this.displayName = displayName;
	}
}

final class KKSCodeMapObject{
	int srcSystemId, srcFieldId, srcTypeId, targetFieldId, targetTypeId;
	public KKSCodeMapObject(int srcSystemId, int srcFieldId, int srcTypeId, int targetFieldId, int targetTypeId){
		this.srcSystemId = srcSystemId;
		this.srcFieldId = srcFieldId;
		this.srcTypeId = srcTypeId;
		this.targetFieldId = targetFieldId;
		this.targetTypeId = targetTypeId;
	}
}
