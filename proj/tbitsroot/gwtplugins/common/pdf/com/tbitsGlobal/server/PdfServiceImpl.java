/**
 * 
 */
package pdf.com.tbitsGlobal.server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import commons.com.tbitsGlobal.utils.client.TbitsExceptionClient;

import pdf.com.tbitsGlobal.client.PdfService;
import pdf.com.tbitsGlobal.shared.PDFConfig;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.plugin.TbitsRemoteServiceServlet;

/**
 * @author Lokesh
 *
 */
public class PdfServiceImpl extends TbitsRemoteServiceServlet implements PdfService{

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	@Override
	public String generatePdf(int aSystemId, int aRequestId) {

		try {
			return PdfServerUtililities.generatePdf(this.getRequest(), aSystemId, aRequestId);
		} catch (TbitsExceptionClient e) {
			e.printStackTrace();
		}
		return "";
	}

	@Override
	public ArrayList<PDFConfig> getPdfConfigList() {
		ArrayList<PDFConfig> pdfConfigList = new ArrayList<PDFConfig>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			PreparedStatement ps = connection.prepareStatement("SELECT * FROM pdf_generation_config_table");
			ResultSet rs = ps.executeQuery();
			if (rs != null){
				while(rs.next()){
					PDFConfig pc = new PDFConfig(rs.getInt("id"), rs.getInt("sys_id"),
											rs.getString("report_template_name"));
					pdfConfigList.add(pc);
				}
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
		
		return pdfConfigList;
	}

}
