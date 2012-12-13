package billtracking.com.tbitsGlobal.server;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.Type;
import billtracking.com.tbitsGlobal.shared.IBillConstants;

import commons.com.tbitsGlobal.utils.client.TbitsTreeRequestData;

public class Process implements IBillConstants {
	private static final int FIRST_STATE_ID = 1;
	private static final int ZERO_STATE_ID = 0;
	private static final String DECISION_FLOW_FORWARD = "forward";
	private static final String DECISION_FLOW_BACKWARD = "backward";
	HashMap<Integer,State> stateMap;
	int processId;



	public Process(int processId) {
		super();
		this.processId = processId;
		this.stateMap=getAllStates(processId);
	}

	/*
	 * gets a process from a request
	 */
	public static  Process getProcess(Request currentRequest){
		int processId=getProcessId(currentRequest);
		return new Process(processId);

	}

	public static  Process getProcess(TbitsTreeRequestData model){
		int processId=getProcessId(model);
		return new Process(processId);

	}

	/*
	 * get all states corresponding to a process
	 */

	private  HashMap<Integer,State> getAllStates(int processId){


		HashMap<Integer,State> allStatesHash=new HashMap<Integer,State>();
		Connection aCon=null; 
		try {
			aCon=DataSourcePool.getConnection();
			PreparedStatement stateCount = aCon.prepareStatement("select count(distinct(state_id)) as state_count from "+process_table_Name+" where process_id=?" );
			stateCount.setString(1,Integer.toString(processId));
			ResultSet rsCount = stateCount.executeQuery();
			int noOfStates=0;
			while(rsCount!=null && rsCount.next()!=false){
				noOfStates=rsCount.getInt("state_count");
			}
			rsCount.close();

			for(int stateId=1;stateId<=noOfStates;stateId++){
				PreparedStatement pstmt = aCon.prepareStatement("select key_data,value_data from "+process_table_Name+" where process_id=? and state_id = ?" );

				pstmt.setString(1, Integer.toString(processId));
				pstmt.setString(2,Integer.toString(stateId));

				HashMap<String,String>actionTable=new HashMap<String,String>();
				ResultSet rs = pstmt.executeQuery();
				while(rs!=null && rs.next()!=false){
					String keyData=rs.getString("key_data");
					String valueData=rs.getString("value_data");
					String newKeyData=Utils.fieldTrimmer(keyData).trim();
					String newValueData=Utils.fieldTrimmer(valueData).trim();
					actionTable.put(newKeyData,newValueData);
				}
				rs.close();
				State state=new State(actionTable,stateId,processId);
				allStatesHash.put(stateId,state);
			}
			aCon.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return allStatesHash;


	}

	/*
	 * gets the table which constitutes how a process id defined
	 */
	private static  HashMap<String,HashMap<String,String>> getProcessIdentificationTable(){


		HashMap<String,HashMap<String,String>> allProcessHash=new HashMap<String,HashMap<String,String>>();
		try {
			Connection aCon= DataSourcePool.getConnection();
			PreparedStatement processCount = aCon.prepareStatement("select count(distinct(process_id)) as process_count from "+process_identification_table_Name);
			ResultSet rsCount = processCount.executeQuery();
			int noOfProcess=0;
			while(rsCount!=null && rsCount.next()!=false){
				noOfProcess=rsCount.getInt("process_count");
			}
			rsCount.close();

			for(int pId=1;pId<=noOfProcess;pId++){
				PreparedStatement pstmt = aCon.prepareStatement("select key_data,value_data from "+process_identification_table_Name+" where process_id=?");

				pstmt.setString(1, Integer.toString(pId));

				HashMap<String,String>processTable=new HashMap<String,String>();
				ResultSet rs = pstmt.executeQuery();
				while(rs!=null && rs.next()!=false){
					String keyData=rs.getString("key_data");
					String valueData=rs.getString("value_data");
					String newKeyData=Utils.fieldTrimmer(keyData).trim();
					String newValueData=Utils.fieldTrimmer(valueData).trim();


					processTable.put(newKeyData,newValueData);
				}
				rs.close();
				allProcessHash.put(Integer.toString(pId), processTable);
			}
			aCon.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return allProcessHash;


	}

	public int getProcessId(){
		return this.processId;
	}

	public State getFirstState(Request currentRequest){
		State zeroState=new State();
		zeroState.setStateId(ZERO_STATE_ID);
		String controlFieldName=StateProcessor.getManualFlowControlField(DECISION_FLOW_FORWARD,this,zeroState);
		if(controlFieldName==null){
			return stateMap.get(FIRST_STATE_ID);
		}
		else{
			String controlFieldValue=currentRequest.get(controlFieldName);
			return StateProcessor.getNextState(controlFieldName,controlFieldValue,DECISION_FLOW_FORWARD,this,zeroState);
		}
	}

	public State getState(Request currentRequest){
		String stateId=currentRequest.get(bill_state_id);
		if(stateId!=null)
			return stateMap.get(Integer.parseInt(stateId));
		else 
			return null;
	}

	public State getState(int stateId){
		return stateMap.get(stateId);
	}

	/*
	 * gets the next state after consulting decision table
	 */
	public State getNextState(Request currentRequest){
		String controlFieldName=StateProcessor.getManualFlowControlField(DECISION_FLOW_FORWARD,this,this.getState(currentRequest));
		if(controlFieldName==null){
			int nextStateId=this.getState(currentRequest).getNextStateId();
			return stateMap.get(nextStateId);
		}
		else{
			String controlFieldValue=currentRequest.get(controlFieldName);
			return StateProcessor.getNextState(controlFieldName,controlFieldValue,DECISION_FLOW_FORWARD,this,this.getState(currentRequest));
		}
	}

	/*
	 * gets the previous state after consulting decision table
	 */

	public State getPrevState(Request currentRequest){
		String controlFieldName=StateProcessor.getManualFlowControlField(DECISION_FLOW_BACKWARD,this,this.getState(currentRequest));
		if(controlFieldName==null){
			int prevStateId=this.getState(currentRequest).getPrevStateId();
			return stateMap.get(prevStateId);
		}
		else{
			String controlFieldValue=currentRequest.get(controlFieldName);
			return StateProcessor.getNextState(controlFieldName,controlFieldValue,DECISION_FLOW_BACKWARD,this,this.getState(currentRequest));
		}
	}

	/*
	 * gets the rejection state after consulting decision table
	 */
	public State getRejState(Request currentRequest){
		String controlFieldName=StateProcessor.getManualFlowControlField(DECISION_FLOW_BACKWARD,this,this.getState(currentRequest));
		if(controlFieldName==null){
			int rejStateId=this.getState(currentRequest).getRejStateId();
			return stateMap.get(rejStateId);
		}
		else{
			String controlFieldValue=currentRequest.get(controlFieldName);
			return StateProcessor.getRejState(controlFieldName,controlFieldValue,DECISION_FLOW_BACKWARD,this,this.getState(currentRequest));
		}
	}


	private static int getProcessId(Request currentRequest) {	
		HashMap<String,HashMap<String,String>> allProHash=getProcessIdentificationTable();
		Set<String> keyset=allProHash.keySet();
		Iterator<String> i=keyset.iterator();
		while(i.hasNext()){
			String pid=i.next();
			HashMap<String,String>proHash=allProHash.get(pid);
			Set<String> e=proHash.keySet();
			Iterator<String> it=e.iterator();
			Boolean allTrue=true;  
			while(it.hasNext()){
				String keyData=it.next().trim();
				Type typeField = (Type)currentRequest.getObject(keyData);
				Boolean match=typeField.getName().equals(proHash.get(keyData));
				allTrue=match&&allTrue;
			}
			if(allTrue)
				return Integer.parseInt(pid);

		}
		return 0;
	}

	private static int getProcessId(TbitsTreeRequestData model) {	
		HashMap<String,HashMap<String,String>> allProHash=getProcessIdentificationTable();
		Set<String> keyset=allProHash.keySet();
		Iterator<String> i=keyset.iterator();
		while(i.hasNext()){
			String pid=i.next();
			HashMap<String,String>proHash=allProHash.get(pid);
			Set<String> e=proHash.keySet();
			Iterator<String> it=e.iterator();
			Boolean allTrue=true;  
			while(it.hasNext()){
				String keyData=it.next().trim();
				String typeFieldName = model.get(keyData);
				Boolean match=typeFieldName.equals(proHash.get(keyData));
				allTrue=match&&allTrue;
			}
			if(allTrue)
				return Integer.parseInt(pid);

		}
		return 0;
	}

}
