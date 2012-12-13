package billtracking.com.tbitsGlobal.server;

import java.io.Serializable;
import java.util.HashMap;

/*
 * Represents the state of a Request
 * consists of state identification and action
 * attributes in Hashtable form
 */
public class State implements IState,Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String NEXT_STEP_ID_OF_LAST_STATE = "-2";
	private static final String PREV_ID_OF_FIRST_STATE = "-1";
	private static final String PREV_ID_OF_SECOND_STATE = "1";
	private static final String PREV_ID_OF_THIRD_STATE = "2";
	private int processId;
	private int stateId;
	private int nextStateId;
	private int prevStateId;
	private int rejStateId;
	
	private HashMap<String,String> stateData;

	public int getRejStateId() {
		return rejStateId;
	}

	public void setRejStateId(int rejStateId) {
		this.rejStateId = rejStateId;
	}


	public int getStateId() {
		return stateId;
	}

	public void setStateId(int StateId) {
		this.stateId = StateId;
	}
	
	public State() {
		// TODO Auto-generated constructor stub
	}


	public State(HashMap<String,String> stateData,int stateId,int processId) {
		super();
		this.stateData = stateData;
		this.stateId=stateId;
		this.processId=processId;

		String nextStateId=stateData.get(next_state_id);
		this.nextStateId=(nextStateId!=null)?(Integer.parseInt(nextStateId)):null;

		String prevStateId=stateData.get(prev_state_id);
		this.prevStateId=(prevStateId!=null)?(Integer.parseInt(prevStateId)):null;

		String rejStateId=stateData.get(reject_state_id);
		this.rejStateId=(rejStateId!=null)?(Integer.parseInt(rejStateId)):null;

	}

	
	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

	public int getNextStateId() {
		return nextStateId;
	}

	public void setNextStateId(int nextStateId) {
		this.nextStateId = nextStateId;
	}

	public int getPrevStateId() {
		return prevStateId;
	}

	public void setPrevStateId(int prevStateId) {
		this.prevStateId = prevStateId;
	}

    public String get(String attribute){
    	return stateData.get(attribute);
    }

	public Boolean isFirstState() {
		Boolean isFirstState=this.get(prev_state_id).equals(PREV_ID_OF_FIRST_STATE);
		return isFirstState;
	}
	public Boolean isSecondState() {
		Boolean isSecondState=this.get(prev_state_id).equals(PREV_ID_OF_SECOND_STATE);
		return isSecondState;
	}
	public Boolean isThirdState() {
		Boolean isThirdState=this.get(prev_state_id).equals(PREV_ID_OF_THIRD_STATE);
		return isThirdState;
	}

	/**
	 * @param currStateData
	 * @return
	 */
	public Boolean isLastState() {
		Boolean isLastState=this.get(next_state_id).equals(NEXT_STEP_ID_OF_LAST_STATE);
		return isLastState;
	}
	
	public Boolean isDecision(){
		return this.get(is_state_decision).equalsIgnoreCase("true");
	}
}
