package transbit.tbits.scheduler;

import javax.servlet.*;

import transbit.tbits.api.Mapper;
import transbit.tbits.indexer.IndexerDaemon;
import uk.org.primrose.pool.standalone.PoolControllerStandalone;

public  class TbitsListener implements ServletContextListener 
{
  
      private ServletContext context = null;
	 	  
	  	 	
	  /*This method is invoked when the Web Application has been removed 
	  and is no longer able to accept requests
	  */
	
	  public void contextDestroyed(ServletContextEvent event)
	  {
	    //Output a simple message to the server's console
	    
	    //TBitsSchedulerClient schedulerClient =  new TBitsSchedulerClient();
	    //schedulerClient.execute("stop");
		this.context = event.getServletContext();

		// delete lock file for indexer
		IndexerDaemon.deleteLockFile();
		
		System.out.println("Shuting down scheduler.");
		TBitsScheduler.Shutdown(true);
		
		System.out.println("Stoping Mapper.");
		Mapper.stop();
		
		System.out.println("Shutting down PoolController.");
		PoolControllerStandalone.shutdown();
		
	  }
	
	  //This method is invoked when the Web Application
	  //is ready to service requests
	
	  public void contextInitialized(ServletContextEvent event)
	  {
	    this.context = event.getServletContext();
	    
	    //delete lock file for indexer
	    IndexerDaemon.deleteLockFile();
	    
	    //start the quartz scheduler
		TBitsScheduler schedular = new TBitsScheduler();
	     schedular.start(); 
	  }
	  
	  public static void main(String[] args)
	  {
		  //start the quartz scheduler
			TBitsScheduler schedular = new TBitsScheduler();
		     schedular.start(); 
	  }
       
}

























