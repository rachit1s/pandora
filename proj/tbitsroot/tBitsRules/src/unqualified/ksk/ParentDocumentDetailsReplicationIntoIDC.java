/**
 * 
 */
package ksk;

import static transbit.tbits.Helper.TBitsConstants.PKG_WEBAPPS;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ksk.KSKUtils;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.DisplayGroup;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.IPreRenderer;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

/**
 * @author lokesh
 *
 */
public class ParentDocumentDetailsReplicationIntoIDC implements IPreRenderer {
	
	//LOG to capture logs in webapps package.
	private static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_WEBAPPS);
	
	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#getSequence()
	 */
	public double getSequence() {
		return 2;
	}

	/* (non-Javadoc)
	 * @see transbit.tbits.domain.IPreRenderer#process(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Hashtable, java.util.ArrayList)
	 */
	public void process(HttpServletRequest request,
			HttpServletResponse response, Hashtable<String, Object> tagTable,
			ArrayList<String> tagList) throws TBitsException {
		
		boolean isRuleApplicable = false;
		
		HttpSession session = request.getSession();
		Request parentRequest = null;
		try{			
			String uri = request.getRequestURI();
			String[] keys = uri.split("/");
			
			isRuleApplicable = KSKUtils.isExistsInString(KSKUtils.IDCBALIST, keys[2]);
			
			if (keys[1].equals("add-subrequest") && isRuleApplicable){
				int requestId = -1;
				requestId = Integer.parseInt(keys[3]);
				BusinessArea ba = BusinessArea.lookupBySystemPrefix(keys[2]);
				parentRequest = Request.lookupBySystemIdAndRequestId(ba.getSystemId(), requestId);
				
				/*ArrayList<Field> dDFields = getDocumentDetailFields(parentRequest);
				if (dDFields != null)
					for(Field ddField : dDFields){						
						String fieldName = ddField.getName();
						String fieldValue = parentRequest.get(fieldName);
						System.out.println("%%%%%%%%%%%%%%%%%FieldName: " + fieldName + ", " + fieldValue);
						tagTable.put(fieldName, fieldValue);
					}		*/	
				
				String parentDocNo = parentRequest.get("DrawingNumber");
				String parentActPer = parentRequest.get("ActualPercentageComplete");
				String parentSepcoNo = parentRequest.get("SEPCODocumentNumber");
				String docEnggType = parentRequest.get("DocEngineeringType");
				String parentDocType = parentRequest.get("DocumentCategory");
				String parentRev = parentRequest.get("Revision");
				String sysDef = parentRequest.get("SystemDefinition");
				String parentSAC = parentRequest.get("Area");
				String parentSepcoClass = parentRequest.get("SEPCOClassification");
				String parentWght = parentRequest.get("Weightage");
				//String parentPackage = parentRequest.get("SystemDefinition");
				
				StringBuffer exFieldsBuffer = new StringBuffer();
				exFieldsBuffer.append("<script type=\"text/javascript\"> \n").append("var drwNo=").append("\"").append(parentDocNo).append("\";")
				.append("\n var docElem = document.getElementById (\"").append("DrawingNumber").append("\");\n docElem.value =drwNo; \n")
				.append("var actPer=").append("\"").append(parentActPer).append("\";").append("\n var docElem = document.getElementById (\"")
				.append("ActualPercentageComplete").append("\");\n docElem.value =actPer; \n");
				
				exFieldsBuffer.append("var sepcoNo=").append("\"").append(parentSepcoNo).append("\";").append("\n var docElem = document.getElementById (\"")
				.append("SEPCODocumentNumber").append("\");\n docElem.value =sepcoNo; \n");
				
				exFieldsBuffer.append("var sysDefinition=").append("\"").append(parentSepcoNo).append("\";")
				.append("\n var docElem = document.getElementById (\"").append("SystemDefinition")
				.append("\");\n docElem.value =sepcoNo; \n");
				
				exFieldsBuffer.append("var weightage=").append("\"").append(parentWght).append("\";").append("\n var docElem = document.getElementById (\"")
				.append("Weightage").append("\");\n docElem.value =weightage; \n");
				
				exFieldsBuffer.append("setSelect(\"").append("Area").append("\",").append("\"").append(parentSAC).append("\");");
				exFieldsBuffer.append("setSelect(\"").append("SEPCOClassification").append("\",").append("\"").append(parentSepcoClass).append("\");");
				
				exFieldsBuffer.append("var sysDef=").append("\"").append(sysDef).append("\";").append("\n var docElem = document.getElementById (\"")
				.append("SystemDefinition").append("\");\n docElem.value =sysDef; \n");
				exFieldsBuffer.append("setSelect(\"").append("DocEngineeringType").append("\",").append("\"").append(docEnggType).append("\");");
				exFieldsBuffer.append("setSelect(\"").append("DocumentCategory").append("\",").append("\"").append(parentDocType).append("\");")
				.append("setSelect(\"").append("Revision").append("\",").append("\"").append(parentRev).append("\");")
				.append("</script>");
				
				tagTable.put("prefillData", exFieldsBuffer.toString().trim());			
				
			}
		} catch (DatabaseException e) {
			LOG.severe("Error occurred while retrieving parent request for IDC");
			session.setAttribute("ExceptionObject", e);
			return;
		} catch (Exception e){
			e.printStackTrace();
		}

	}
	
	private ArrayList<Field> getDocumentDetailFields(Request request){
		
		Connection connection = null;
		ArrayList<Field>fieldList = new ArrayList<Field>();
		try{
			connection = DataSourcePool.getConnection();
			DisplayGroup dg = DisplayGroup.lookupBySystemIdAndDisplayName(request.getSystemId(), "Document Details");
			PreparedStatement ps = connection.prepareStatement("select * from fields where sys_id=? and display_group=?");
			ps.setInt(1, request.getSystemId());
			ps.setInt(2, dg.getId());
			ResultSet rs = ps.executeQuery();
			if(rs!=null){
				while (rs.next() != false) {
					Field field = Field.createFromResultSet(rs);
					fieldList.add(field);
				}
			}
		} catch (SQLException sqle) {
			StringBuilder message = new StringBuilder();

			message.append("An exception occured while retrieving the fields.").append("\nSystem Id: ").append(request.getSystemId()).append("\n");

			//throw new DatabaseException(message.toString(), sqle);
		} catch (DatabaseException e) {
			e.printStackTrace();
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (SQLException sqle) {
					LOG.warn("Exception while closing the connection:", sqle);
				}

				connection = null;
			}
		}
		return fieldList;
	}

}
