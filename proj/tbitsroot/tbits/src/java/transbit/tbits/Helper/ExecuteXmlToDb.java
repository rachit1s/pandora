package transbit.tbits.Helper;

import java.io.File;


import transbit.tbits.common.Configuration;


public class ExecuteXmlToDb  {

	private static XMLtoDatabase xtod = new XMLtoDatabase();
//	private static InsertToDb insert = new InsertToDb();
		
	public static void main(String[] args) {
		
			if(args.length<2) {
				System.out.println("Please Type the Absolute Paths of " +
						"Old Configuration files And New Configuration File");
				System.out.println("Java ExecuteXmlToDb oldConfigurationFile newConfigurationFile");
				return;
			}
			
		System.out.println(args[0]+"  "+args[1]);
		 File oldConfigFile = Configuration.findPath(args[0]);
		 File newConfigFile = Configuration.findPath(args[1]);
		 
		
		if(oldConfigFile == null)  {
			 System.out.println("Old Configuration File Does Not Exists");
			 return;
		 	}
		 
		 if(newConfigFile == null) {
			 System.out.println("New Configuration File Does Not Exists");
			 return;
		 	}
		
		 try {
			 boolean isSuccess = xtod.getJobs(oldConfigFile,newConfigFile);

			 if(isSuccess == true) {
				 System.out.println("Jobs was Loaded Successfully");
			 	}
			 else {
				 System.out.println("Jobs Could Not be loaded");
			 	}
		 }
		catch(Exception e){
			System.out.println("Request Could Not Be Completed");
			e.printStackTrace();
		}
		
	}

}
