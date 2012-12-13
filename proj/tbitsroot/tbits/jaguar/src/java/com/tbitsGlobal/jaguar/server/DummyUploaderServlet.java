package com.tbitsGlobal.jaguar.server;

import java.io.File;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsFileInfo;
import transbit.tbits.common.Utilities;

@SuppressWarnings("unchecked")
public class DummyUploaderServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private int requestId = 0;
	private int actionId = 0;
	private String folderHint = null;
	File attachmentBase;
	
	public DummyUploaderServlet() {
		super();
		attachmentBase = new File("/home/sourabh/projects/tmp");
//        attachmentBase = new File(APIUtil.ourAttachmentLocation);
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.getWriter().println("hi");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		folderHint = request.getParameter("folderhint");
		try
		{
			requestId = Integer.parseInt(request.getParameter("requestid"));
		}
		catch(Exception e){}

		try
		{
			actionId = Integer.parseInt(request.getParameter("actionid"));
		}
		catch(Exception e){}

		File parentDir = attachmentBase;
		if(!parentDir.exists())
			parentDir.mkdirs();
		
		parentDir = prepareAttachmentFolder(folderHint, parentDir);
		
		
		// Create a factory for disk-based file items
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// Set factory constraints
		//factory.setSizeThreshold(yourMaxMemorySize);
		File repotmp = new File(APIUtil.getTMPDir());
		//File repotmp = new File(tBitsTmpLoc, "repotmp");
		System.out.println("The repository: " + repotmp.getAbsolutePath());
		factory.setRepository(repotmp);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Set overall request size constraint
		//upload.setSizeMax(yourMaxRequestSize);

		// Parse the request
		try {
			List items = upload.parseRequest(request);
			
			// Process the uploaded items
			Iterator iter = items.iterator();
			while (iter.hasNext()) {
			    FileItem item = (FileItem) iter.next();
			    // Process a file upload
				if (!item.isFormField()) {
				    String fileName = item.getName();
				    String proposedfileName = requestId + "-" + actionId + "-" 
					+ fileName.replaceAll("[^A-Za-z0-9-\\._\\+]+", "_");
		            File fTarget = generateUniqTargetFile(parentDir,
							proposedfileName);
				    
				    try {
						item.write(fTarget);
						long size = item.getSize();
						//response.getWriter().println(size);
						//Now put it in DB
						String relative = attachmentBase.toURI().relativize(fTarget.toURI()).getPath();
						int id = insertIntoDB(relative, fileName, item.getSize());
						response.getWriter().println(size + "-" +id);

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Actual File: " + fTarget.getAbsolutePath());
				}
			}
		} catch (FileUploadException e) {
			e.printStackTrace();
		}
		finally{
			System.out.println("Finished.");
		}
	}

	private File generateUniqTargetFile(File parentDir, String proposedfileName) {
		int dotIndex = proposedfileName.lastIndexOf(".");
		String proposedPrefix = null;
		String proposedSuffix = ".tmp";
		if(dotIndex > -1)
		{
			proposedPrefix = proposedfileName.substring(0, dotIndex);
			proposedSuffix = proposedfileName.substring(dotIndex + 1);
		}
		else
		{
			proposedPrefix = proposedfileName;
		}
		
		File fTarget = new File(parentDir, proposedPrefix + "." + proposedSuffix);					
		int counter = 1;
		while(fTarget.exists())
		{
			fTarget = new File(parentDir, proposedPrefix + counter++ + "." + proposedSuffix);
		}
		return fTarget;
	}
	
	/**
	 * Gets a directory relative to the parentDir and based on folder hint.
	 * @param folderHint the folder to be made inside the parent director. This folder will be sanitized.
	 * @param parentDir Generall  the attachment folder.
	 * @return
	 */
	private File prepareAttachmentFolder(String folderHint,
			File parentDir) {
		/* Tries to create the sub folder based on the folderHint. */
		if(folderHint != null)
		{
			folderHint = folderHint.replaceAll("[^A-Za-z0-9-\\._\\+]+", "_");
			if((folderHint.trim().length() != 0))
			{
				File newParentDir = new File(parentDir, folderHint);
				if(!newParentDir.exists())
				{
					boolean wasCreated = false;
					try
					{
						wasCreated = newParentDir.mkdir();
					}
					catch(Exception e)
					{
					}
					
					if(!wasCreated)
					{
						parentDir = newParentDir;
					}
				}
				else
					parentDir = newParentDir;
			}
		}
		return parentDir;
	}
	
	/**
	 * Copies file into repository.
	 * @param src
	 * @param sysPrefixHint - This helps creating a new directory. Can be blank/null.
	 * @param requestIdHint - this helps in prefixing the file name. Can be blank/null.
	 * @param actionIdHint - this helps in prefixing the file name. Can be blank/null.
	 * @return The returned object contain the repoId
	 */
	public AttachmentInfo copyIntoRepository(File src)
	{
		return pushIntoRepository(src, false);
	}
	
	/**
	 * Moves file into repository. Use this method if don't know actionId, requestId, sysId
	 * @param src
	 * @return The returned object contain the repoId
	 */
	public AttachmentInfo moveIntoRepository(File src)
	{
		return pushIntoRepository(src, true);
	}
	
	private AttachmentInfo pushIntoRepository(File src, boolean isMove)
	{
	
		File parentDir = attachmentBase;
		if(!parentDir.exists())
			parentDir.mkdirs();
		
		parentDir = prepareAttachmentFolder(folderHint, parentDir);
		
		
		  String proposedfileName = requestId + "-" + actionId + "-" 
			+ src.getName().replaceAll("[^A-Za-z0-9-\\._\\+]+", "_");
	      
	      File fTarget = generateUniqTargetFile(parentDir,
					proposedfileName);
		    
		    try {
		    	if(isMove)
		    	{
		    		boolean isRenamed = src.renameTo(fTarget);
		    		if(!isRenamed)
		    		{
		    			//copy
		    			Utilities.copyFile(src, fTarget);
		    			
		    			//delete
		    			{
		    				if(!src.delete())
		    				{
		    					System.out.println("Unable to delete file: " + src.getAbsolutePath());
		    				}
		    			}
		    		}
		    	}
		    	else {
		    		//copy file
		    		Utilities.copyFile(src, fTarget);
				}
		    	
				//Now put it in DB
				String relative = attachmentBase.toURI().relativize(fTarget.toURI()).getPath();
				int id = insertIntoDB(relative, src.getName(), src.length());
				AttachmentInfo attInfo = new AttachmentInfo();
				attInfo.name = src.getName();
				attInfo.repoFileId  = id;
				attInfo.size = (int) src.length();
				return attInfo;
	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
	public static int insertIntoDB(String relative, String fileName, long fileSize) throws DatabaseException {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("insert into file_repo_index (id, location, name, create_date, size) values (?, ?, ?, ?, ?)");
			int id = getUniqRepoId();
			stmt.setInt(1, id);
			stmt.setString(2, relative);
			stmt.setString(3, fileName);
			Date d = new Date();
			Timestamp t = new Timestamp(d.getTime());
			stmt.setTimestamp(4, t, Calendar.getInstance(TimeZone.getTimeZone("GMT")));
			
			stmt.setLong(5, fileSize);
			
			return id;
		} catch (SQLException e) {
			throw new DatabaseException("Unable to insert file info in the db.", e);
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
	
	private static int getUniqRepoId() throws SQLException
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
	/*
	 * Returns the location of file wrt to repoId. This location is wrt to the attachments folder.
	 * 
	 */
	public static String getFileLocation(int fileId) throws DatabaseException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select location from file_repo_index where id = ? ");
			stmt.setInt(1, fileId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String location = rs.getString("location");
				return location;
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw new DatabaseException("UNable to get the file location.", e);
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
	
	/**
	 * Gets the file details
	 * @param sysid
	 * @param requestId
	 * @param actionId
	 * @param requestFileId
	 * @param fieldId
	 * @return
	 * @throws SQLException
	 */
	public static TBitsFileInfo getFileInfo(int sysid, int requestId, int actionId, int requestFileId, int fieldId) throws SQLException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			StringBuilder sb = new StringBuilder();
			sb.append("select top 1 isnull(attachment, fri.name) name, fri.location from versions v ");
			sb.append("JOIN file_repo_index fri on fri.id = v.file_id ");
			sb.append("where v.sys_id = ? and v.request_id = ? and v.request_file_id = ? and v.field_id = ? ");
			if(actionId != -1)
				sb.append(" and v.action_id <= ? ");
			sb.append("order by action_id DESC");
			PreparedStatement stmt = conn
			.prepareStatement(sb.toString());
	
			stmt.setInt(1, sysid);
			stmt.setInt(2, requestId);
			stmt.setInt(3, requestFileId);
			stmt.setInt(4, fieldId);
			if(actionId != -1)
				stmt.setInt(5, actionId);
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String location = rs.getString("location");
				String name = rs.getString("name");
				return new TBitsFileInfo(name, location);
			} else {
				System.out.println("No such file.");
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
	
	public static int getRepoFileId(int sysid, int requestId, int actionId, int requestFileId, int fieldId) throws SQLException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			
			StringBuilder sb = new StringBuilder();
			sb.append("select top 1 file_id from versions v ");
			sb.append("where v.sys_id = ? and v.request_id = ? and v.request_file_id = ? and v.field_id = ? ");
			if(actionId != -1)
				sb.append(" and v.action_id <= ? ");
			sb.append("order by action_id DESC");
			PreparedStatement stmt = conn
			.prepareStatement(sb.toString());
	
			stmt.setInt(1, sysid);
			stmt.setInt(2, requestId);
			stmt.setInt(3, requestFileId);
			stmt.setInt(4, fieldId);
			if(actionId != -1)
				stmt.setInt(5, actionId);
			
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int location = rs.getInt("file_id");
				return location;
			} else {
				System.out.println("No such file.");
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
