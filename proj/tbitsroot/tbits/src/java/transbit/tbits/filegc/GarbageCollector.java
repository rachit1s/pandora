package transbit.tbits.filegc;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import transbit.tbits.api.APIUtil;
import transbit.tbits.api.AttachmentInfo;
import transbit.tbits.common.Configuration;
import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.DraftConfig;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.User;
import transbit.tbits.domain.UserDraft;
import transbit.tbits.exception.TBitsException;

/*
 * 
 */
public class GarbageCollector implements Runnable {
	public static TBitsLogger LOG	= TBitsLogger.getLogger("transbit.tbits.filegc");
	
	private static final long aDayinSecs = 24 * 60 * 60;
	private static final long anHourinSecs = 60 * 60;
	
	private static GarbageCollector instance = null; 
	private Hashtable<File, Long> filesMarkedForDeletion = new Hashtable<File, Long>();
	private Hashtable<File, Long> folderMarkedForDeletion = new Hashtable<File, Long>();
	private List<IRepoFileDeletionFilter> repoFileDeleteionFilters = new ArrayList<IRepoFileDeletionFilter>();
	
	private GarbageCollector()
	{
		//add the default locations tmp, build/tmp, build/webapps/tmp
		File appHome = Configuration.getAppHome();
		
		if(appHome != null && appHome.exists()){
			File tmpDir = new File(appHome.getParentFile().getPath() + "/tmp");
			if(tmpDir.exists())
				folderMarkedForDeletion.put(tmpDir, aDayinSecs);

			File buildTmpDir = new File(appHome.getPath() + "/tmp");
			if(buildTmpDir.exists())
				folderMarkedForDeletion.put(buildTmpDir, aDayinSecs);
			
			File webappsTmpDir = new File(appHome.getPath() + "/webapps/tmp");
			if(webappsTmpDir.exists())
				folderMarkedForDeletion.put(webappsTmpDir, aDayinSecs);
			
			File dashboardImagesDir = new File(appHome.getPath() + "/webapps/web/images/dashboard_images");
			if(dashboardImagesDir.exists())
				folderMarkedForDeletion.put(dashboardImagesDir, aDayinSecs);
			
//			File reportEngineDir = new File(appHome.getPath() + "/birt-runtime/ReportEngine/tmp");
		}else{
			LOG.error("App Home not found . Can not continue");
		}
		//add the 
		
	}
	
	public static GarbageCollector getInstance()
	{
		if(instance == null)
		{
			instance = new GarbageCollector();
		}
		return instance;
	}
	
	public void markForDeletion(File f, Long secondsAfterCreation)
	{
		Long existing = filesMarkedForDeletion.get(f);
		
		if((existing == null) || (existing < secondsAfterCreation))
		{
			filesMarkedForDeletion.put(f, secondsAfterCreation);
		}
	}
	
	public void unmarkForDeletion(File f)
	{
		if(filesMarkedForDeletion.contains(f))
			filesMarkedForDeletion.remove(f);
	}
	
	public void addTempFileLocation(File folder, Long secondsAfterCreation)
	{
		Long existing = folderMarkedForDeletion.get(folder);
		
		if((existing == null) || (existing < secondsAfterCreation))
		{
			folderMarkedForDeletion.put(folder, secondsAfterCreation);
		}
	}
	
	public void removeTempFileLocation(File folder)
	{
		if(folderMarkedForDeletion.contains(folder))
			folderMarkedForDeletion.remove(folder);
	}
	
	public void addRepoFileDeletionFilter(IRepoFileDeletionFilter listener)
	{
		if(!repoFileDeleteionFilters.contains(listener))
			repoFileDeleteionFilters.add(listener);
	}
	public void removeRepoFileDeletionFilter(IRepoFileDeletionFilter listener)
	{
		if(repoFileDeleteionFilters.contains(listener))
			repoFileDeleteionFilters.remove(listener);
	}
	
	public void run() {
		//get the orphan repo files based on the fileCreationDate
		Hashtable<Integer, File> orphanFiles = null;
		try {
			orphanFiles = gettBitsOrphanFiles();
		} catch (TBitsException e) {
			LOG.info("",(e));
		}
		
		if(orphanFiles != null){
			//check against drafts
			checkDrafts(orphanFiles);
			
			//run thru listener
			for(IRepoFileDeletionFilter filter : repoFileDeleteionFilters){
				orphanFiles = filter.filterFilesToBeDeleted(orphanFiles);
			}
			
			
			//mark repo files for deletion
			if(orphanFiles != null){
				for(int repoId : orphanFiles.keySet()){
					File file = orphanFiles.get(repoId);
					if(file != null){
						filesMarkedForDeletion.put(file, new Long(0));
						LOG.info("Orphan File having repo id : " + repoId + " found at location : " + file.getPath() + " has been marked for deletion");
					}
				}
			}
		}
		
		//delete the marked files based on secondsAfterCreation 
		for(File file : filesMarkedForDeletion.keySet()){
			if(file.exists() && file.isFile()){
				long secondsAfterCreation = filesMarkedForDeletion.get(file);
				long lastModified = file.lastModified();
				long now = (new Date()).getTime();
				if(now - lastModified > secondsAfterCreation * 1000){
					if(file.delete())
						LOG.info("SuccessFully Deleted File --- > " + file.getPath());
					else
						LOG.info("Could not Delete File --- > " + file.getPath());
				}
			}
		}
		
		//delete the files in temp locations
		for(File file : folderMarkedForDeletion.keySet()){
			if(file.exists() && file.isDirectory()){
				long secondsAfterCreation = folderMarkedForDeletion.get(file);
				long lastModified = file.lastModified();
				long now = (new Date()).getTime();
				if(now - lastModified > secondsAfterCreation * 1000){
					if(deleteDir(file, false))
						LOG.info("SuccessFully Deleted Directory --- > " + file.getPath());
					else
						LOG.info("Could not Delete Directory --- > " + file.getPath());
				}
			}
		}
	}
	
	private boolean deleteDir(File aDir, boolean deleteIteslf){
		if(aDir.exists() && aDir.isDirectory()){
			File[] files = aDir.listFiles();
			for(File file : files){
				if(file.exists()){
					if(file.isDirectory()){
						if(!deleteDir(file, true))
							return false;
					}else{
						if(!file.delete())
							return false;
					}
				}
			}
			if(deleteIteslf)
				return aDir.delete();
			else
				return true;
		}
		return false;
	}
	
	private Hashtable<Integer, File> gettBitsOrphanFiles() throws TBitsException
	{
		Hashtable<Integer, File> orphanFiles = new Hashtable<Integer, File>();
		
		File attachmentBase = new File(APIUtil.getAttachmentLocation());
		
		if(attachmentBase != null){
			Connection conn = null;
			try {
				conn = DataSourcePool.getConnection();
				
				String sql = "select id, location, name from file_repo_index where id not in (select file_id from versions)";
				
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery();
				
				if(rs != null){
					while(rs.next()){
						int repoId = rs.getInt("id");
						String location = rs.getString("location");
						String name = rs.getString("name");
						
						File file = new File(attachmentBase.getPath() + "/" + location);
						if(file.exists())
							orphanFiles.put(repoId, file);
						else{
							LOG.warn("File with repo id : " + repoId + " named : " + name + " was not found at location : " + file.getPath());
						}
					}
				}
				ps.close();
			} catch (SQLException e) {
				LOG.info("",(e));
				throw new TBitsException(e);
			}finally{
				if (conn != null) {
					try {
						conn.close();
					} catch (SQLException e) {
						LOG.info("",(e));
						throw new TBitsException(e);
					}
				}
			}
		}else{
			LOG.warn("Attachment directory was not found");
		}
		return orphanFiles;
	}
	
	private void checkDrafts(Hashtable<Integer, File> orphanFiles){
		LOG.info("Checking in drafts.................");
		List<User> users = User.getAllUsers();
		Hashtable<Integer, List<Field>> fieldTable = new Hashtable<Integer, List<Field>>();
		for(User user : users){
			try {
				List<UserDraft> drafts = UserDraft.lookupByUserId(user.getUserId());
				for(UserDraft draft : drafts){
					int sysId = draft.getSystemId();
					if(!fieldTable.containsKey(sysId)){
						List<Field> fields = Field.lookupBySystemId(sysId);
						List<Field> attachmentFields = new ArrayList<Field>();
						for(Field field : fields){
							if(field.getDataTypeId() == DataType.ATTACHMENTS)
								attachmentFields.add(field);
						}
						
						fieldTable.put(sysId, attachmentFields);
					}
					List<Field> attachmentFields = fieldTable.get(sysId);
					try{
						Hashtable<String, String> fieldValues = DraftConfig.xmlDeSerialize(draft.getDraft());
						for(Field field : attachmentFields){
							String value = fieldValues.get(field.getName());
							if(value != null){
								Collection<AttachmentInfo> attachments = AttachmentInfo.fromJson(value);
								for(AttachmentInfo attachment : attachments){
									if(orphanFiles.contains(attachment.getRepoFileId()))
									{
										orphanFiles.remove(attachment.getRepoFileId());
										LOG.info("Orphan File having repo id : " + attachment.getRepoFileId() + " has been filtered by draft id : " + draft.getDraftId() 
											+ " field id : " + field.getFieldId());
									}
								}
							}
						}
					}catch(Exception e){
						LOG.info("",(e));
					}
				}
			} catch (DatabaseException e) {
				LOG.info("",(e));
			} catch (Exception e) {
				LOG.info("",(e));
			}
		}
	}
	
	public static void main(String[] args) {
		GarbageCollector.getInstance().run();
		
		LOG.info("Garbage Collection finished...!!!");
		System.exit(1);
	}
}
