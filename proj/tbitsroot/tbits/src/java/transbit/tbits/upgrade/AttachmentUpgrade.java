package transbit.tbits.upgrade;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import transbit.tbits.TVN.FileAction;
import transbit.tbits.TVN.WebdavConstants;
import transbit.tbits.TVN.WebdavUtil;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.Uploader;
import transbit.tbits.config.Attachment;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

public class AttachmentUpgrade implements IUpgrade {

	public boolean upgrade(Connection conn, String folder, String sysType)
			throws SQLException, DatabaseException, TBitsException {
		// Read all the requests' attachment info
		boolean isSuccessful = false;
		ArrayList<BusinessArea> bas;
		try {
			bas = BusinessArea.getAllBusinessAreas();

			clearVersions(conn);

			for (BusinessArea ba : bas) {
				String requestQuery = "select * from requests where sys_id = ? order by request_id ASC";
				PreparedStatement rps = conn.prepareStatement(requestQuery);
				rps.setInt(1, ba.getSystemId());
				ResultSet rrs = rps.executeQuery();
				while (rrs.next()) {
					int requestId = rrs.getInt("request_id");
					String attachmentsXML = rrs.getString("attachments");
					int maxActionId = rrs.getInt("max_action_id");
					String subject = rrs.getString("subject");

					String actionsQuery = "select * from actions where sys_id = ? and request_id = ? order by action_id ASC";
					PreparedStatement ps = conn.prepareStatement(actionsQuery);
					ps.setInt(1, ba.getSystemId());
					ps.setInt(2, requestId);
					ResultSet rs = ps.executeQuery();
					while (rs.next()) {
						int actionId = rs.getInt("action_id");
						System.out.println("SYSID: " + ba.getSystemId() + ", REQUEST_ID: " + requestId + ", ACTION_ID: " + actionId);
						ArrayList<Attachment> xmlAtts = null;
						String xml = rs.getString("attachments");
						if ((xml != null) && (xml.trim().length() != 0)) {
							try {
								xmlAtts = Attachment.getAttachments(xml);
							} catch (Exception exp) {
								exp.printStackTrace();
								throw new TBitsException(exp);
							}
						}
						ArrayList<FileAction> fileActions = new ArrayList<FileAction>();
						ArrayList<AttachmentInfo> finalAttachments = new ArrayList<AttachmentInfo>();
						if (xmlAtts != null) {
							for (Attachment xmlAtt : xmlAtts) {
								AttachmentInfo attInfo = new AttachmentInfo();
								attInfo.name = xmlAtt.getDisplayName();
								attInfo.size = (int) xmlAtt.getSizeInBytes();
								attInfo.requestFileId = APIUtil
										.getAndCreateRequestFileId(ba
												.getSystemId(), requestId);

								String relAttLoc = ba.getSystemPrefix()
										.toLowerCase()
										+ "/" + xmlAtt.getName();
								int repoFileId = insertIntoDB(relAttLoc, relAttLoc, 
															xmlAtt.getSizeInBytes());
								attInfo.repoFileId = repoFileId;

								fileActions.add(new FileAction(attInfo,
										WebdavConstants.FILE_ADDED, 22));
								finalAttachments.add(attInfo);
							}
						}
						int version = WebdavUtil.updateVersion(conn, ba
								.getSystemId(), requestId, subject,
								actionId, fileActions, true, null, 0, true);

						// Now Upgrade the attachments in Request
						Request.updateAttachments(conn, ba.getSystemId(),
								requestId, actionId, AttachmentInfo
										.toJson(finalAttachments));
					}
				}
			}
			isSuccessful = true;
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch (TBitsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		}
		return isSuccessful;
	}
	
	private void clearVersions(Connection conn) throws SQLException {
		try {
			String query = "delete from versions";
			PreparedStatement ps = conn.prepareStatement(query);
			ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw e;
		}
	}
	
	private static int insertIntoDB(String relative, String fileName, long size) 
											throws DatabaseException {
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt = conn.prepareStatement("insert into file_repo_index (id, location, name, create_date, size) values (?, ?, ?, ?, ?)");
			int id = getUniqRepoId();
			stmt.setInt(1, id);
			stmt.setString(2, relative);
			stmt.setString(3, fileName);
			Date d = new Date();
			Timestamp t = new Timestamp(d.getTime());
			stmt.setTimestamp(4, t, Calendar.getInstance(TimeZone.getTimeZone("GMT")));
			stmt.setLong(5, size);
			
			int ret = stmt.executeUpdate();
			return id;
		} 
		catch (SQLException e) {
			try {
				conn.rollback();
			} 
			catch (SQLException e1) {
				e1.printStackTrace();
			}
		throw new DatabaseException("Unable to insert file info in the db.", e);
		} 
		finally {
			if(conn != null)
				try {
					conn.close();
				} 
				catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
	
	public static int getUniqRepoId() throws SQLException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			CallableStatement stmt = conn
					.prepareCall("stp_getAndIncrMaxId ?");
			stmt.setString(1, "file_repo_index");
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int id = rs.getInt("max_id");
				return id;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw e;
		} finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
