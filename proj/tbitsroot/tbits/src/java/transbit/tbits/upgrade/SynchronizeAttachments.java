package transbit.tbits.upgrade;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;

import transbit.tbits.TVN.WebdavConstants;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.exception.TBitsException;

public class SynchronizeAttachments implements IUpgrade{

	public static void synchronize(Connection conn, ArrayList<Integer> sysIds, ArrayList<Integer> requestIds) throws SQLException, DatabaseException
	{
		StringBuilder extSql = new StringBuilder();
		if(sysIds.size() > 0)
		{
			extSql.append(" AND v.sys_id in (");
			for(int sysId:sysIds)
			{
				extSql.append(sysId).append(",");
			}
			extSql.deleteCharAt(extSql.length() - 1);
			extSql.append(")");
		}
		if(requestIds.size() > 0)
		{
			extSql.append(" AND v.sys_id in (");
			for(int reqId:requestIds)
			{
				extSql.append(reqId).append(",");
			}
			extSql.deleteCharAt(extSql.length() - 1);
			extSql.append(")");
		}
		String sql = 
				"select " +
				"	v.sys_id, v.request_id, v.field_id, v.action_id,v.request_file_id,v.file_action, v.attachment name,fri.location location, fri.id fildId, fri.size " +
				"from " +
				"	versions v " +
				"JOIN file_repo_index fri " +
				"on fri.id = v.file_id where v.request_file_id != 0 " + extSql.toString() + 
				"order by sys_id ASC, request_id ASC, field_id ASC, action_id ASC";
		Statement stmt = conn.createStatement();
		ResultSet resultSet = stmt.executeQuery(sql);
		ArrayList<AttachmentInfo> attachments = new ArrayList<AttachmentInfo>();
		int prevSysId = 0;
		int prevRequestId = 0;
		int prevFieldId = 0;
		int prevActionId = 0;
		if(!resultSet.next())
			return;
		
		boolean isFirst = true;
		int totalRecords = 0;
		int totalUpdates = 0;
		do
		{
			int sysId = resultSet.getInt("sys_id");
			int requestId = resultSet.getInt("request_id");
			int fieldId = resultSet.getInt("field_id");
			int actionId = resultSet.getInt("action_id");

			int requestFileId = resultSet.getInt("request_file_id");
			
			String name = resultSet.getString("name");
			String location = resultSet.getString("location");
			int fileId = resultSet.getInt("fildId");
			String fileAction = resultSet.getString("file_action");
			int fileSize = resultSet.getInt("size");
			
			AttachmentInfo attInfo = new AttachmentInfo();
			attInfo.name = name;
			attInfo.repoFileId = fileId;
			attInfo.requestFileId = requestFileId;
			attInfo.size = fileSize;
			
			boolean isSameField = false;
			if((prevSysId == sysId) && (prevRequestId == requestId) && (prevFieldId == fieldId))
			{
				isSameField = true;
			}
			
			boolean isEnd = !resultSet.next();
			
			if((!isSameField && !isFirst) || isEnd)
			{
				totalUpdates++;
				addAttachments(conn, attachments, prevSysId, prevRequestId, prevFieldId,
						prevActionId);
				//clear the attachments
				attachments.clear();
			}
			
			if(isEnd)
			{
				System.out.println("Hit the dead end");
				break;
			}
			
			if(fileAction.equals(WebdavConstants.FILE_ADDED))
			{
				attachments.add(attInfo);
			}
			else
			{
				removeFile(attachments, requestFileId);
				if(fileAction.equals(WebdavConstants.FILE_MODIFIED))
				{
					attachments.add(attInfo);
				}
			}
			
			//move current to previous
			prevSysId = sysId;
			prevRequestId = requestId;
			prevFieldId = fieldId;
			prevActionId = actionId;
			totalRecords++;
			
			if(isFirst)
				isFirst = false;
		} while(true);
		System.out.println("Total Records: " + totalRecords);
		System.out.println("Total Updates: " + totalUpdates);
	}

	private static void removeFile(ArrayList<AttachmentInfo> attachments,int requestFileId) {
		AttachmentInfo found = null;
		for(AttachmentInfo ai:attachments)
		{
			if(ai.requestFileId == requestFileId)
			{
				found = ai;
				break;
			}
		}
		if(found != null)
		{
			attachments.remove(found);
		}
	}

	private static void addAttachments(Connection conn,
			ArrayList<AttachmentInfo> attachments, int sysId, int requestId,
			int fieldId, int actionId) throws DatabaseException {
		System.out.println("Updating: [sys_id: " + sysId + ", requestId: " + requestId + ", fieldId: " + fieldId + ", actionId: " + actionId + "]");
		String attStr = AttachmentInfo.toJson(attachments);
		System.out.println("Attachments: " + attStr);
		//commit
		Field f = Field.lookupBySystemIdAndFieldId(sysId, fieldId);
		if(f == null)
			throw new DatabaseException("Unable to find field corresponding to sysId: " + sysId + ", fieldId: " + fieldId, new SQLException());
		if(!f.getIsExtended())
		{
			Request.updateAttachments(conn, sysId, requestId, actionId, attStr);
		}
		else
		{
			Request.updateAttachmentsExt(conn, sysId, requestId, fieldId, actionId, attStr);
		}
	}
	public static void main(String[] args) {
		ArrayList<BusinessArea> bas = new ArrayList<BusinessArea>();
		if(args.length == 0)
		{
			System.out.println("Syntax error.");
			System.out.println("Usage: SyncAttFromVersionToRequests *|<sys_prefix> <sys_prefix> ...");
			System.out.println("\t* means all business areas");
		}
		else 
		{
			for(String arg:args)
			{
				if(arg.equals("*"))
				{
					try {
						bas = BusinessArea.getAllBusinessAreas();
					} catch (DatabaseException e) {
						e.printStackTrace();
						return;
					}
					break;
				}
				else
				{
					try
					{
						BusinessArea ba = BusinessArea.lookupBySystemPrefix(arg);
						if(ba != null)
						{
							bas.add(ba);
						}
					}
					catch(Exception exp)
					{
						System.out.println("Unable to find the business area: " + arg);
						exp.printStackTrace();
					}
				}
			}
		}
		
		ArrayList<Integer> baIds = new ArrayList<Integer>();
		for(BusinessArea ba:bas)
		{
			baIds.add(ba.getSystemId());
		}
		
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			conn.setAutoCommit(false);
			
			Date d = new Date();
			SynchronizeAttachments.synchronize(conn, baIds, new ArrayList<Integer>());
			Date ed = new Date();
			System.out.println("Time Taken: "+ (ed.getTime() - d.getTime()));
			System.out.println("Finished.");
			conn.commit();
			System.out.println("Committed.");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		finally
		{
			if(conn != null)
			{
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public boolean upgrade(Connection conn, String folder, String sysType)
			throws SQLException, DatabaseException, TBitsException {
		synchronize(conn, new ArrayList<Integer>(), new ArrayList<Integer>());
		return true;
	}
}
