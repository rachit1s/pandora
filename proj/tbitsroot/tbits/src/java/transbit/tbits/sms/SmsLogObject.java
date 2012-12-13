package transbit.tbits.sms;

/*
  Each object of this class represents a row of database table containing sms logs.  
* */

import java.sql.Timestamp;

public class SmsLogObject {
private	String cellNo;
private	int sysId;
private	int requestId;
private Timestamp timestamp;
int actionId;
int userId;

    public int getActionId() {
        return actionId;
    }

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Timestamp getTimestamp() {
	return timestamp;
}
public void setTimestamp(Timestamp timestamp) {
	this.timestamp = timestamp;
}
public String getCellNo() {
	return cellNo;
}
public void setCellNo(String cellNo) {
	this.cellNo = cellNo;
}
public int getRequestId() {
	return requestId;
}
public void setRequestId(int requestId) {
	this.requestId = requestId;
}
public int getSysId() {
	return sysId;
}
public void setSysId(int sysId) {
	this.sysId = sysId;
}


}
