package tatapowerCOMRM;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import transbit.tbits.common.DataSourcePool;
import transbit.tbits.common.DatabaseException;

import com.google.gson.Gson;

public final class TataPowerComRmUtils {

	public static class AssingeeMap{
		String userLogin, categoryName, requestTypeName;
		public AssingeeMap( String categoryName, String requestTypeName, String userLogin){
			this.categoryName = categoryName;
			this.requestTypeName = requestTypeName;
			this.userLogin = userLogin;
		}
	}
	
	public static String getAssigneeMap() throws DatabaseException{
		String assigneesMap = "";
		ArrayList<AssingeeMap> assigneeMapList = new ArrayList<AssingeeMap>();
		Connection connection = null;
		try {
			connection = DataSourcePool.getConnection();
			String queryString = "SELECT * FROM " +  CategoryAndSeverityBasedAssigneeSubstitutionInUpdate.COMMINSIONING_REVIEW_ASSIGNEES_MAP;
			PreparedStatement ps = connection.prepareStatement(queryString);
			ResultSet resultSet = ps.executeQuery();
			if (resultSet != null){
				while(resultSet.next()){
					String categoryName = resultSet.getString(1);
					String requestTypeName = resultSet.getString(2);
					String userLogin = resultSet.getString(3);
					assigneeMapList.add(new AssingeeMap(categoryName, requestTypeName, userLogin));
				}					
			}
			resultSet.close();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseException("Database exception occurred.", e);
		}	
		Gson gson = new Gson();
		assigneesMap = gson.toJson(assigneeMapList);		
		return assigneesMap;
	}
	
	public static String showError( String errorMsg )
	 {
		 String html = "<script type='text/javascript'> \n" +		 		
		 		" function prefillException() \n" +
		 		" { \n" +
		 		"   // alert( 'prefillException called' ) ; \n" +
		 		"	showAutomaticRestrictions( \" " + errorMsg + " \" ) ;\n" +
		 		" } \n" +
		 		" YAHOO.util.Event.addListener( window, 'load', prefillException ) ; \n" +
		 		" </script> \n"	; 
		 
		 return html ;		 
	 }

}
