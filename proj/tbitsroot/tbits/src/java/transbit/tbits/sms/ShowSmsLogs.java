package transbit.tbits.sms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import transbit.tbits.common.DataSourcePool;

public class ShowSmsLogs {

    

    public ArrayList<SmsLogObject> getlogs(int sysId, int month, int year){
     	ArrayList<SmsLogObject>logList = new ArrayList<SmsLogObject>();
    	String dateStart = Integer.toString(year) + "-"+ Integer.toString(month) + "-" + Integer.toString(1);
        String dateEnd = Integer.toString(year) + "-"+ Integer.toString(month+1) + "-" + Integer.toString(1);

        Connection aCon = null ;
        try {
    		aCon = DataSourcePool.getConnection();

            String query = "select * from sms_log where 1=1 and sys_id =? and date >=? and date <? ORDER by date DESC";
            PreparedStatement preparedStatement = aCon.prepareStatement(query);
    		preparedStatement.setInt(1,sysId);
    		preparedStatement.setString(2, dateStart);
            preparedStatement.setString(3, dateEnd);
    		ResultSet logsResultSet = preparedStatement.executeQuery();

            while(logsResultSet.next()){
             	SmsLogObject smsLogObject = new SmsLogObject();
             	smsLogObject.setSysId(logsResultSet.getInt(2));
             	smsLogObject.setRequestId(logsResultSet.getInt(1));
             	smsLogObject.setCellNo(logsResultSet.getString(3));
              	smsLogObject.setTimestamp(logsResultSet.getTimestamp(4));
                smsLogObject.setUserId(logsResultSet.getInt(5));
                smsLogObject.setActionId(logsResultSet.getInt(6));
                logList.add(smsLogObject);
            }
            //System.out.println("smsLogObject "+logList.get(0).getCellNo());
            return logList;
    	}catch(SQLException e){
    		e.printStackTrace();
    		return logList;
    	}finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {

                }
            }
        }

	
	}

    public ArrayList<SmsBaCount> getSmsCount(int month, int year){
        ArrayList<SmsBaCount> smsCountList = new ArrayList<SmsBaCount>();
        String dateStart = Integer.toString(year) + "-"+ Integer.toString(month) + "-" + Integer.toString(1);
        String dateEnd = Integer.toString(year) + "-"+ Integer.toString(month+1) + "-" + Integer.toString(1);

        Connection aCon = null ;
      try{
          aCon = DataSourcePool.getConnection();
          String query = "select sys_id, count(*) total_smscount from sms_log where date >=? and date <? group by sys_id";
              PreparedStatement PS = aCon.prepareStatement(query);
              PS.setString(1, dateStart);
            PS.setString(2, dateEnd);
          ResultSet RS = PS.executeQuery();
          while(RS.next()){
                   SmsBaCount smsBaCount = new SmsBaCount();
               smsBaCount.setBa(RS.getInt(1));
               smsBaCount.setSmsCount(RS.getInt(2));
              smsCountList.add(smsBaCount);
           }
      }catch(SQLException e){
        	e.printStackTrace();

      }finally {
            if (aCon != null) {
                try {
                    aCon.close();
                } catch (SQLException sqle) {
                    //System.out.println("sqle = " + sqle);
                    sqle.printStackTrace();
                }
            }
        }
     return smsCountList;
    }

    public static void main(String[] args) {
		ShowSmsLogs s = new ShowSmsLogs();
        s.getSmsCount(6,2007);
//        System.out.println("Time = " +   s.getlogs(1,6,2007).size());

    }

}
