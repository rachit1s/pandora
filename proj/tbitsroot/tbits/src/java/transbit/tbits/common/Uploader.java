package transbit.tbits.common;

/**
 * TODO: 
 * *. Exception handling had to be corrected
 * *. Set the correct paths
 * *. Upload in the DB
 * *. Add the location hint (essentially a BA).
 * 
 */

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.jfree.util.Log;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.Helper.TBitsHelper;
import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.dms.AttachmentUtils;
import transbit.tbits.domain.Field;
import transbit.tbits.webapps.AttachmentFieldInfo;
import transbit.tbits.webapps.WebUtil;

/**
 * Servlet implementation class Uploader
 */
public class Uploader extends HttpServlet {
	
	public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_COMMON);
	
	private static final long serialVersionUID = 1L;
	protected int requestId = 0;
	protected int actionId = 0;
	protected String folderHint = null;
//	protected File attachmentBase;
	private static int securityCodeBase = 10000;
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Uploader() {
        super();
        
        //TODO: Get the fileLocation from properties instead. This is quite dangerous. 
        //TODO: Once servlet doesnt initialize, even if you keep on setting the attachment property it wouldnt work without restart.
//        attachmentBase = new File(APIUtil.getAttachmentLocation());
    }

    /**
     * 
     * @param requestId - this helps in prefixing the file name. Can be blank/null.
     * @param actionId - this helps in prefixing the file name. Can be blank/null.
     * @param folderHint - The BA prefix, ideally. This helps creating a new directory. Can be blank/null. 
     */
	public Uploader(int requestId, int actionId, String folderHint) {
		this();
		this.requestId = requestId;
		this.actionId = actionId;
		this.folderHint = folderHint;
	}


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) 
						throws ServletException, IOException {
		doPost(request, response);
	}

	
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
						throws ServletException, IOException {
		folderHint = request.getParameter("folderhint");
		try
		{
			requestId = 0;
			requestId = Integer.parseInt(request.getParameter("requestid"));
		}
		catch(Exception e){}

		try
		{
			actionId = 0;
			actionId = Integer.parseInt(request.getParameter("actionid"));
		}
		catch(Exception e){}

		
		File attachmentBase = new File(APIUtil.getAttachmentLocation());
		File parentDir = attachmentBase;
		if(!parentDir.exists())
			parentDir.mkdirs();
		
		parentDir = prepareAttachmentFolder(folderHint, parentDir);
		
		
		
		// Set factory constraints
		//factory.setSizeThreshold(yourMaxMemorySize);
//		File repotmp = new File(APIUtil.ourTmpLocation);
		//File repotmp = new File(tBitsTmpLoc, "repotmp");
//		System.out.println("The repository: " + repotmp.getAbsolutePath());
//		factory.setRepository(repotmp);

		// Set overall request size constraint
		//upload.setSizeMax(yourMaxRequestSize);
		
		String source = request.getHeader("X-Source");
		
		if(source == null || !source.toLowerCase().equals("gwt")){
		// Parse the request
			try {
				// Create a factory for disk-based file items
				DiskFileItemFactory factory = new DiskFileItemFactory();
	
				// Create a new file upload handler
				ServletFileUpload upload = new ServletFileUpload(factory);
				
				List items = upload.parseRequest(request);
				
				// Process the uploaded items
				Iterator iter = items.iterator();
				while (iter.hasNext()) {
				    FileItem item = (FileItem) iter.next();
				    // Process a file upload
					if (!item.isFormField()) {
					    String fileName = item.getName();
					    //Since IE send the full path, we need to strip it out
	//				    if (fileName.matches("^[a-zA-Z]:\\\\.+")) {
							int sp = fileName.lastIndexOf("\\") + 1;
							if(sp < fileName.length())
								fileName = fileName.substring(sp);
	//					}
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
							int id = writeFilePropsIntoDB(relative, fileName, fTarget);
							response.getWriter().println(size + "-" +id);
	
						} catch (DatabaseException e) {
							LOG.error("",(e));
						} catch (Exception e) {
							LOG.error("",(e));
						}
						System.out.println("Actual File: " + fTarget.getAbsolutePath());
					}
				}
			} catch (FileUploadException e) {
				LOG.error("",(e));
			}
		}else{
			String fileName = request.getHeader("X-Filename");
		    
			File tmp = generateUniqTargetFile(parentDir, fileName);
			
		    InputStream in = request.getInputStream();
		    if (in != null) {
		    	FileOutputStream out = new FileOutputStream(tmp);
		    	byte[] buffer = new byte[65536];
		
		    	int l;
		    	while ((l = in.read(buffer)) != -1) {
		    		out.write(buffer, 0, l);
		     	}
		    	out.close();
		    	in.close();
		    	
		    	int size = request.getContentLength();
		    	System.out.println("Saved " + size + " bytes to " + tmp.getAbsolutePath());
		      
		    	String relative = attachmentBase.toURI().relativize(tmp.toURI()).getPath();
				try {
					int id = writeFilePropsIntoDB(relative, fileName, tmp);
					response.setStatus(HttpServletResponse.SC_OK);
				    response.setContentType("text/plain");
				    response.getWriter().print(id);
				} catch (DatabaseException e) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				    response.setContentType("text/plain");
				    response.getWriter().println(TBitsLogger.getStackTrace(e));
					LOG.error("",(e));
				}
		    }
		
		    
		}
	}

	public static File generateUniqTargetFile(File parentDir, String proposedfileName) {
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
	public static File prepareAttachmentFolder(String folderHint,
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
						Log.error("Unable to create the folder : " + newParentDir.getPath(), e);
					}
					
					if(wasCreated)
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
	public AttachmentInfo copyIntoRepository(File src, String fileName)
	{
		return pushIntoRepository(src, false, fileName);
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
		return pushIntoRepository(src, false, null);
	}
	
	/**
	 * Moves file into repository. Use this method if don't know actionId, requestId, sysId
	 * @param src
	 * @return The returned object contain the repoId
	 */
	public AttachmentInfo moveIntoRepository(File src, String fileName)
	{
		return pushIntoRepository(src, true, fileName);
	}
	
	/**
	 * Moves file into repository. Use this method if don't know actionId, requestId, sysId
	 * @param src
	 * @return The returned object contain the repoId
	 */
	public AttachmentInfo moveIntoRepository(File src)
	{
		return pushIntoRepository(src, true, null);
	}
	
	private AttachmentInfo pushIntoRepository(File src, boolean isMove, String fileName)
	{
		File attachmentBase = new File(APIUtil.getAttachmentLocation());
		File parentDir = new File(APIUtil.getAttachmentLocation());
		if(!parentDir.exists())
			parentDir.mkdirs();
	
		if(fileName == null)
			fileName = src.getName();
		
		parentDir = prepareAttachmentFolder(folderHint, parentDir);
		
		
		  String proposedfileName = requestId + "-" + actionId + "-" 
			+ fileName.replaceAll("[^A-Za-z0-9-\\._\\+]+", "_");
          
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
				int id = writeFilePropsIntoDB(relative, fileName, fTarget);
				AttachmentInfo attInfo = new AttachmentInfo();
				attInfo.name = fileName;
				attInfo.repoFileId  = id;				
				attInfo.size = (int) fTarget.length();				
				return attInfo;

			} 
		    catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return null;
	}
	
	//====================================================================================

	/**
	 * Writes all the properties of the specified file to the file_repo_index table.
	 */
	public static int writeFilePropsIntoDB(String relative, String fileName, File toBeInserted) throws DatabaseException{
		
		// Find the sha1 hash of the file to be inserted.
		String hash = null;
		try {
			hash = HashUtilities.computeHash(toBeInserted, "SHA1");
		} 
		catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			// TODO: if sha1 is not found, see if it can be replaced by md5.
		}
		// Size of the file
		long size = toBeInserted.length();
		// 5 digit random number for security code.
		float securityCodeGenerator = new Random(new Date().getTime()).nextInt(securityCodeBase);
		securityCodeGenerator *= securityCodeGenerator;
		securityCodeGenerator /= new Random(new Date().getTime()).nextInt(securityCodeBase/2);
		int securityCode = (int)(securityCodeBase + (securityCodeGenerator%(securityCodeBase*10)));
		
		return insertIntoDB(relative, fileName, size, hash, securityCode);
	}
	
	//====================================================================================

	/**
	 * To be called only through the writeFilePropsIntoDB method.
	 * 
	 * @param relative
	 * @param fileName
	 * @param size
	 * @param hash
	 * @param securityCode
	 * @return the generated repository file index
	 * @throws DatabaseException
	 */
	private static int insertIntoDB(String relative, String fileName, long size, String hash, int securityCode) 
						throws DatabaseException {
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("insert into file_repo_index (id, location, name, create_date, size, hash, security_code) values (?, ?, ?, ?, ?, ?, ?)");
			int id = getUniqRepoId();
			stmt.setInt(1, id);
			stmt.setString(2, relative);
			stmt.setString(3, fileName);
			Date d = new Date();
			Timestamp t = new Timestamp(d.getTime());
			stmt.setTimestamp(4, t, Calendar.getInstance(TimeZone.getTimeZone("GMT")));
			stmt.setLong(5, size);
			stmt.setString(6, hash);
			stmt.setInt(7, securityCode);
			
			int ret = stmt.executeUpdate();
			return id;
		} 
		catch (SQLException e) {
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
	
	//====================================================================================

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
	
	
	
	
	/*
     * Returns the name of file wrt to repoId.
     * 
     */
    public static String getFileName(int fileId) throws DatabaseException
    {
        Connection conn = null;
        try {
            conn = DataSourcePool.getConnection();
            PreparedStatement stmt = conn
                    .prepareStatement("select name from file_repo_index where id = ? ");
            stmt.setInt(1, fileId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                return name;
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            throw new DatabaseException("UNable to get the file name.", e);
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
	 * Returns the location of file wrt to repoId, hash and security code. 
	 * This location is wrt to the attachments folder.
	 * 
	 */
	public static String getFileLocation(int fileId, String hash, int securityCode) throws DatabaseException
	{
		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			PreparedStatement stmt = conn
					.prepareStatement("select location, hash, security_code from file_repo_index where id = ? ");
			stmt.setInt(1, fileId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				String location = rs.getString("location");
				if(rs.getString(2).equals(hash) && rs.getInt(3)==securityCode)
					return location;
				else
					throw new SQLException();
			} else {
				throw new SQLException();
			}
		} catch (SQLException e) {
			throw new DatabaseException("Unable to get the file location.", e);
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
	
	
	/*
	 * Returns the location of files for the external user email
	 * 
	 */
	public static ArrayList<ActionFileInfo> getActionFileInfos(int sysId, int requestId, int actionId) throws DatabaseException
	{
		ArrayList<ActionFileInfo> actionFiles = new ArrayList<ActionFileInfo>();

		Connection conn = null;
		try {
			conn = DataSourcePool.getConnection();
			String sql = "select v.file_action,v.request_file_id,v.file_id,v.field_id,v.attachment,fri.location,fri.size from versions v " +
			"Join file_repo_index fri on v.file_id = fri.id " +
			"where v.sys_id = ? and v.request_id = ? and v.action_id = ?";
			PreparedStatement stmt = conn
					.prepareStatement(sql);
			
			stmt.setInt(1, sysId);
			stmt.setInt(2, requestId);
			stmt.setInt(3, actionId);
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				String location = rs.getString("location");
				String name = rs.getString("attachment");
				int file_id = rs.getInt("file_id");
				String file_action = rs.getString("file_action");
				int request_file_id = rs.getInt("request_file_id");
				int field_id = rs.getInt("field_id");		
				int size = rs.getInt("size"); 
				actionFiles.add(new ActionFileInfo( sysId, requestId, actionId, name, file_action, field_id, file_id, request_file_id,location,size));
			}
			return actionFiles;
		} catch (SQLException e) {
			throw new DatabaseException("Unable to get the files for request : " + sysId + "#"+ requestId +"#" + actionId , e);
		} finally {
			if(conn != null)
				try {
					conn.close();
				} catch (SQLException e) {				
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * Gets the file details
	 * @param sysid
	 * @param requestId
	 * @param actionId action id or -1 if 
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

	public void setFolderHint(String folderHint) {
		this.folderHint = folderHint;
	}

	public String getFolderHint() {
		return folderHint;
	}

}

