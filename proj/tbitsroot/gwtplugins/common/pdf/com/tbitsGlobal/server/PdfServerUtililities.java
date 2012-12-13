/**
 * 
 */
package pdf.com.tbitsGlobal.server;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.eclipse.birt.core.exception.BirtException;

import pdf.com.tbitsGlobal.shared.PDFConfig;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.domain.User;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.report.TBitsReportEngine;

/**
 * @author Lokesh
 *
 */
public class PdfServerUtililities {

	public static final String PDF 				= ".pdf";
	public static final Object REQUEST_HANDLER 	= "RequestHandler";
	private static final String SYS_ID 			= "sys_id";
	private static final String REQUEST_ID 		= "request_id";
	private static final String ASSIGNEE 		= "Assignee";	
	
	public static String generatePdf(HttpServletRequest httpRequest, int aSystemId,
			int aRequestId) throws TbitsExceptionClient{
			
		try{
			Request request = Request.lookupBySystemIdAndRequestId(aSystemId, aRequestId);
			return generateTransmittalNoteUsingBirt(httpRequest, request);
		} catch (DatabaseException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Database error occurred while generating pdf.", e);
		} catch (TBitsException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Error occurred while generating pdf. " + e.getMessage(), e);
		} catch (BirtException e) {
			e.printStackTrace();
			throw new TbitsExceptionClient("Birt exception occurred while generating pdf.", e);
		}
	}
	
	
	public static String generateTransmittalNoteUsingBirt(HttpServletRequest httpRequest, 
			Request request)
	throws BirtException, TBitsException {
		
		String pdfUrl = "";
		TBitsReportEngine tBitsEngine = TBitsReportEngine.getInstance();
		Map<Object, Object> reportVariables = new HashMap<Object, Object>();
		Map<String, Object> reportParams = new HashMap<String, Object>();
		
		reportParams.put(REQUEST_ID, request.getRequestId());
		reportParams.put(SYS_ID, request.getSystemId());
		
		File tempDir = Configuration.findPath("webapps/tmp");
		String outputFileName = request.getSystemId() + "";		
		String pdfFilePath = tempDir + File.separator + outputFileName + "_" + request.getRequestId() + PDF;
		File outFile = new File(pdfFilePath);
		
		reportVariables.put(REQUEST_HANDLER, request);						
		ArrayList<RequestUser> assignees = (ArrayList<RequestUser>)request.getAssignees();
		ArrayList<User> assigneeList = new ArrayList<User>();
		if ((assignees != null) && assignees.size() != 0){
			for(RequestUser ru : assignees){
				User u = null;
				try {
					u = User.lookupAllByUserId(ru.getUserId());
				} catch (DatabaseException e) {
					e.printStackTrace();
				}
				if (u != null)
					assigneeList.add(u);
			}
			reportVariables.put(ASSIGNEE, assigneeList);
		}
		
		PDFConfig pc = getPdfConfigBySystemId(request.getSystemId());
		if (pc != null){
			File generatedPDFFile = tBitsEngine.generatePDFFile(pc.getReportTemplateName(), reportVariables,
										reportParams, outFile);
			pdfUrl = getPdfUrl(httpRequest, generatedPDFFile);
		}
		return pdfUrl;
	}

	private static String getPdfUrl(HttpServletRequest httpRequest,
			File generatedPDFFile) {
		String prot = (httpRequest.getProtocol().toLowerCase().contains("https") ? "https" : "http" );
		String toreturn = prot +"://" + httpRequest.getServerName() + ":"
								+ httpRequest.getServerPort()
								+ httpRequest.getContextPath() + "/tmp/" + generatedPDFFile.getName();
		return toreturn;
	}
	
	public static PDFConfig getPdfConfigBySystemId (int aSystemId) {
		PDFConfig pdfConfig = null;
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM pdf_generation_config_table WHERE sys_id=?");
			ps.setInt(1, aSystemId);
			ResultSet rs = ps.executeQuery();
			if ((rs != null)&& (rs.next())){
				pdfConfig = new PDFConfig(rs.getInt("id"), rs.getInt("sys_id"),
											rs.getString("report_template_name"));
				return pdfConfig;
			}
			rs.close();

		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			if (connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}

		return pdfConfig;
	}

	
}
