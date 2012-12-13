package transbit.tbits.sms;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import transbit.tbits.common.Timestamp;
import transbit.tbits.common.DataSourcePool;
import java.sql.Timestamp;

/*
* This class performs the task of persisting the data corresponding to sms-log
* */
public class SMSLogs {

    /** ruleId, cell no, BA, date
     * @param args
     */
    String cellNo;
    int sysId;
    int requestId;
    int actionId;
    int userId;

    public SMSLogs(int sysId, int requestId, String cellNo, int userId, int actionId ) {
        this.requestId = requestId;
        this.cellNo = cellNo;
        this.sysId = sysId;
        this.actionId = actionId;
        this.userId = userId;
        //System.out.println("Constructor: ");

    }


    public void logSms(){

//        System.out.println("cellNo = " + cellNo);
//        System.out.println("sysId = " + sysId);
//        System.out.println("userId = " + userId);
        Connection aCon = null;
        Timestamp smsDate = new Timestamp(new Date().getTime());
        try {
            aCon = DataSourcePool.getConnection();
            //System.out.println(" A ");
            PreparedStatement preparedStatement = aCon.prepareStatement("INSERT INTO sms_log (request_id,  sys_id, cell_no, date, user_id, action_id) VALUES (?, ?, ?, ?, ?, ?)");
            //System.out.println(" B ");
            preparedStatement.setInt(1,requestId);
            preparedStatement.setInt(2,sysId);
            preparedStatement.setString(3,cellNo);
            preparedStatement.setTimestamp(4, smsDate);
            preparedStatement.setInt(5, userId);
            preparedStatement.setInt(6, actionId);

            //System.out.println(" D ");
            int i = preparedStatement.executeUpdate();
            //System.out.println("i "+i);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        finally {
			if (aCon != null) {
				try {
					aCon.close();
				} catch (SQLException sqle) {
				}
			}
		}

    }
    public static void main(String[] args) {
        SMSLogs smsLogs = new SMSLogs(2,2,"9898989899", 1, 1);
        smsLogs.logSms();
    }

}
