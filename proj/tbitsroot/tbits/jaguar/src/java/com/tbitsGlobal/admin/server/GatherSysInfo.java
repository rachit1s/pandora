package com.tbitsGlobal.admin.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

import org.hyperic.sigar.FileSystem;
import org.hyperic.sigar.NetInfo;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import transbit.tbits.common.Configuration;

import com.tbitsGlobal.admin.client.modelData.SysInfoClient;

/**
 * Gather the system and jvm information.
 * The class primarily uses 'sigar' api's for interacting with the os
 * and collecting required values.
 * @author devashish
 *
 */
public class GatherSysInfo {

	private static String DB_PROPERTIES 	= "DB Properties";
	private static String JVM_PROPERTIES 	= "JVM Properties";
	private static String FS_PROPERTIES		= "Filesystem Properties";
	private static String MISC				= "Misc Properties";
	private static String NETWORK_PROPERTIES= "Network Properties";
	
	protected ArrayList<SysInfoClient> sysPropertiesList;
	protected Sigar	sigar;
	
	public GatherSysInfo(){
		sysPropertiesList 	= new ArrayList<SysInfoClient>();
		sigar				= new Sigar();
		
		gatherValues(sigar);
	}
	
	/**
	 * Reads the file tbits.dbpool and gather required information from there
	 */
	protected void gatherDBValues(){
		FileInputStream finDBpool = null;
		try{
			try {
				File etcFolder = Configuration.findPath("etc");
				String dbPoolfilepath = etcFolder.getAbsolutePath() + "/tbits.dbpool";
				finDBpool = new FileInputStream(new File(dbPoolfilepath));
				Properties propDb = new Properties();
				propDb.load(finDBpool);
				
				Enumeration enProperties = propDb.propertyNames();
				String key = "";
				while(enProperties.hasMoreElements()){
					key = (String) enProperties.nextElement();
					String value = propDb.getProperty(key);
					//-------Insert into model data-------------//
					SysInfoClient sysinfo = new SysInfoClient();
					sysinfo.setGroup(DB_PROPERTIES);
					sysinfo.setProperty(key);
					sysinfo.setPropertyValue(value);
					
					sysPropertiesList.add(sysinfo);
				}
		
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch(IOException e){
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
			}
		}finally{
			if (finDBpool != null) {
                try {
                	finDBpool.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
		}
	}
	
	/**
	 * Gathers properties associated with running instance of jvm.
	 */
	protected void gatherJVMProperties(){
		
		Properties propRuntime = new Properties();
		propRuntime = System.getProperties();
		
		Enumeration enRuntimeProps = propRuntime.propertyNames();
		String key = "";
		while(enRuntimeProps.hasMoreElements()){
			key = (String) enRuntimeProps.nextElement();
			String value = propRuntime.getProperty(key);

			SysInfoClient sysinfo = new SysInfoClient();
			sysinfo.setGroup(JVM_PROPERTIES);
			sysinfo.setProperty(key);
			sysinfo.setPropertyValue(value);
			
			sysPropertiesList.add(sysinfo);
		}
	}
	
	protected void gatherNetworkProperties(Sigar sigar){
		NetInfo netinfo = new NetInfo();
		try {
			netinfo.gather(sigar);
			
			SysInfoClient sysinfo = new SysInfoClient();			
			sysinfo.setGroup(NETWORK_PROPERTIES);			
			sysinfo.setProperty("Default Gateway");
			sysinfo.setPropertyValue(netinfo.getDefaultGateway());
			sysPropertiesList.add(sysinfo);
			
			SysInfoClient hostname = new SysInfoClient();
			hostname.setGroup(NETWORK_PROPERTIES);
			hostname.setProperty("Host-Name");
			hostname.setPropertyValue(netinfo.getHostName());
			sysPropertiesList.add(hostname);
			
			SysInfoClient primaryDNS = new SysInfoClient();
			primaryDNS.setGroup(NETWORK_PROPERTIES);
			primaryDNS.setProperty("Primary DNS");
			primaryDNS.setPropertyValue(netinfo.getPrimaryDns());
			sysPropertiesList.add(primaryDNS);

		} catch (SigarException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	/**
	 * Gathers any misc. properties which do not lie under predefined sections
	 */
	protected void gatherMiscProperties(){
		SysInfoClient sysinfo = new SysInfoClient();
		sysinfo.setGroup(MISC);
		sysinfo.setProperty("Home");
		sysinfo.setPropertyValue(Configuration.getAppHome().toString());
		
		sysPropertiesList.add(sysinfo);
	}
	
	/**
	 * Gather filesystem properties. 
	 * @param sigar
	 */
	protected void gatherFileSystemProperties(Sigar sigar){
		FileSystem[] fs = null;
		try {
			fs = sigar.getFileSystemList();
			
			for(FileSystem f : fs){

				SysInfoClient fsInfo = new SysInfoClient();
				fsInfo.setGroup(FS_PROPERTIES);
				fsInfo.setProperty(f.toString());
				
				String usageInfo = "";
				usageInfo = usageInfo + "Total:\t" + sigar.getFileSystemUsage(f.toString()).getTotal() + "\t\t\tAvail:\t" + 
								sigar.getFileSystemUsage(f.toString()).getFree() + "\t\t\t%Free:\t" + 
									((1 - sigar.getFileSystemUsage(f.toString()).getUsePercent()) * 100);
				
				fsInfo.setPropertyValue(usageInfo);
				sysPropertiesList.add(fsInfo);
	
			}
		} catch (SigarException e) {
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	
	}
	
	public void gatherValues(Sigar sigar){
		//----------------------------Home--------------------------------//
		gatherMiscProperties();
		//--------------------------DB properties-------------------------//
		gatherDBValues();	
		//-------------------Runtime properties---------------------------//
		gatherJVMProperties();
		//--------------------Network properties--------------------------//
		gatherNetworkProperties(sigar);
		//---------------------Filesystem properties---------------------//
		gatherFileSystemProperties(sigar);
	}
	
	public ArrayList<SysInfoClient> getSysInfoList(){
		return sysPropertiesList;
	}
}
