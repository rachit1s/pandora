package kskbilltracking;
import static kskbilltracking.BillConstants.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.exception.TBitsException;

public class BillProperties {

	public static Hashtable<String,String> getActionTableByStepId(String processId,String stepId){

		Hashtable<String,String>actionTable=new Hashtable<String,String>();
		try {
			Connection aCon= DataSourcePool.getConnection();
			PreparedStatement pstmt = aCon.prepareStatement("select key_data,value_data from "+Db_table_Name+" where process_id=? and step_id = ?" );
			pstmt.setString(1,processId);
			pstmt.setString(2,stepId);

			ResultSet rs = pstmt.executeQuery();
			while(rs!=null && rs.next()!=false){
				actionTable.put(rs.getString("key_data"),rs.getString("value_data"));
			}
			rs.close();
			aCon.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return actionTable;


	}

	public static Hashtable<String,Hashtable<String,String>> getActionTableByAllSteps(int processId){

		
		Hashtable<String,Hashtable<String,String>> allStepsHash=new Hashtable<String,Hashtable<String,String>>();
		try {
			Connection aCon= DataSourcePool.getConnection();
			for(int stepId=1;stepId<8;stepId++){
				PreparedStatement pstmt = aCon.prepareStatement("select key_data,value_data from "+Db_table_Name+" where process_id=? and step_id = ?" );

				pstmt.setString(1, Integer.toString(processId));
				pstmt.setString(2,Integer.toString(stepId));
				
				Hashtable<String,String>actionTable=new Hashtable<String,String>();
				ResultSet rs = pstmt.executeQuery();
				while(rs!=null && rs.next()!=false){
					actionTable.put(rs.getString("key_data"),rs.getString("value_data"));
				}
				rs.close();
				allStepsHash.put(Integer.toString(stepId), actionTable);
				}
			aCon.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return allStepsHash;


	}

	public static void insertRow(Connection aCon,String pluginName,String processId,String stepId,String keyData,String valueData){
		PreparedStatement ps;
		try {
			ps = aCon.prepareStatement("INSERT INTO "+Db_table_Name+
			"(plugin_name,process_id,step_id,key_data,value_data) VALUES  (?,?,?,?,?);");
			ps.setString(1,pluginName);
			ps.setString(2,processId);
			ps.setString(3,stepId);
			ps.setString(4,keyData);
			ps.setString(5,valueData);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public static void updateRow(Connection aCon,String processId,String stepId,String keyData,String valueData){
		PreparedStatement ps;
		try {
			ps = aCon.prepareStatement("update "+Db_table_Name+
			" set value_data=? where process_id=? and step_id=? and key_data=?");
			ps.setString(1,valueData);
			ps.setString(2,processId);
			ps.setString(3,stepId);
			ps.setString(4,keyData);
			ps.execute();
			ps.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	


//	public static Hashtable<String,String> getStepHashByStepId(int stepId) throws TBitsException{
////		public static final String Bill_Stores_head ="gprao";
//		//			public static final String Bill_Site_head="prasad.c";
//		//			public static final String Bill_Procurement_head="ajay";
//		//			public static final String Bill_Budgeting_head="gvsrao";
//		//			public static final String Bill_Finance_head="mvrao";
//
//		Hashtable<String,String>stepHash=new Hashtable<String,String>();
//		switch (stepId){
//		case 1: stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//				stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//				stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		case 2: stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//				stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//				stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		case 3: stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//		        stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//		        stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		case 4:	stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//				stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//				stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		case 5:	stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//				stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//				stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		case 6:	stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//				stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//				stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		case 7:	stepHash.put(Db_step_Duration,"");stepHash.put(Db_step_Assignees,"");
//				stepHash.put(Db_step_loggers,"");stepHash.put(Db_step_Dep_Head,"");
//				stepHash.put(Db_step_pending_with,"");stepHash.put(Db_step_Dep_Receipt_Date,"");
//				stepHash.put(Db_step_Dep_Acknowledge_Date,"");stepHash.put(Db_step_Decision,"");break;
//		default: throw new TBitsException("invalide step");		
//		}
//	 return stepHash;
//	}
	public static void main(String[] args){

		try {
			Connection aCon = DataSourcePool.getConnection();





			//			public static final String Bill_Stores_head ="gprao";
			//			public static final String Bill_Site_head="prasad.c";
			//			public static final String Bill_Procurement_head="ajay";
			//			public static final String Bill_Budgeting_head="gvsrao";
			//			public static final String Bill_Finance_head="mvrao";
			//			1	Documentation Cell ganesh.b
			//			2	Stores
			//			3	User Department
			//			4	Site Head
			//			5	SCM
			//			6	Budgetting
			//			7	F&A



			//			ArrayList<String> stepParams=new ArrayList<String>();           
			//			stepParams.add(Db_step_Duration);
			//			stepParams.add(Db_step_Assignees);
			//			stepParams.add(Db_step_loggers);
			//			stepParams.add(Db_step_Dep_Head);
			//			stepParams.add(Db_step_pending_with);
			//			stepParams.add(Db_step_Dep_Receipt_Date);
			//			stepParams.add(Db_step_Dep_Acknowledge_Date);
			//            stepParams.add(Db_step_Decision);

			Hashtable<String,String>stepHash=new Hashtable<String,String>();

			stepHash.put(Db_step_Duration,"1");            
			stepHash.put(Db_step_Assignees,"2");           
			stepHash.put(Db_step_loggers,"3");             
			stepHash.put(Db_step_Dep_Head,"4");            
			stepHash.put(Db_step_pending_with,"5");        
			stepHash.put(Db_step_Dep_Receipt_Date,"6");    
			stepHash.put(Db_step_Dep_Acknowledge_Date,"7");
			stepHash.put(Db_step_Decision,"8");    
			
			String[][] durationMatrix={
					                {"1","1","2","1","0","2","14"},
									{"1","0","0","0","4","2","14"},
					                {"1","1","2","1","1","1","14"},
					                {"1","0","0","0","4","2","14"},
					                {"1","0","4","0","0","2","14"},
					                {"1","0","3","1","0","2","14"},
					                {"1","0","3","1","0","2","14"},
					                {"1","0","3","1","0","2","14"}
									};
			
			String[][] validMatrix={
	                {"1","1","1","1","0","1","1"},
					{"1","0","0","0","1","1","1"},
	                {"1","1","2","1","1","1","14"},
	                {"1","0","0","0","4","2","14"},
	                {"1","0","4","0","0","2","14"},
	                {"1","0","3","1","0","2","14"},
	                {"1","0","3","1","0","2","14"},
	                {"1","0","3","1","0","2","14"}
					};  

			int processId;
			int stepId;
			for (processId=0;processId<8;processId++){


				for(stepId=0;stepId<7;stepId++)
				{
//					
			//		System.out.println(durationMatrix[processId][stepId]);
					//Enumeration<String> keys = stepHash.keys();
//					while(keys.hasMoreElements()){
//						//System.out.println("key:"+keys.nextElement()+" value:"+stepHash.get(keys.nextElement()));
//						String key=keys.nextElement();
//						String value=stepHash.get(key);
//						//System.out.println("key:"+key+" value:"+value);
	//					insertRow(aCon,Db_plugin_name, Integer.toString(processId),Integer.toString(stepId),
		//						key,value);
//					int x=processId+1;
//					int y=stepId+1;
//					updateRow(aCon, Integer.toString(x),Integer.toString(y),
//							"duration",durationMatrix[processId][stepId]);
					}
				}
			


			aCon.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



	}





}
