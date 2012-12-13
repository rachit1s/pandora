package transbit.tbits.scheduler;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.config.XMLParserUtil;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.webapps.WebUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class CronExpressionTester {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String expression = "";
		if(args.length > 0)
		{
//			for (int i = 0; i < args.length; i++) {
				expression += args[0];
//			}
		}
		if (args.length == 0) {
			System.out.println("Enter Expression: ");
			InputStreamReader isr = new InputStreamReader(System.in);
			BufferedReader br = new BufferedReader(isr);
			try {
				expression = br.readLine();
			} catch (IOException ioe) {
				System.out.println(ioe.getMessage());
			}
		}
		try {

			CronExpression cronExpression = new CronExpression(expression);
			System.out.println("Expression: \"" + cronExpression + "\"");
			printNextFiveExecutions(cronExpression);
		} catch (ParseException e) {
			System.out.println("Invalid Expression:" + expression);
		}
	}
	
	private static boolean isCronExpression(String cronExpression){
		return CronExpression.isValidExpression(cronExpression);
	}
	
	public static void printNextFiveExecutions(CronExpression cronExpression) {
		int NUM_OF_EVENTS = 10;
		for(Date d:getNextExecutions(cronExpression, NUM_OF_EVENTS))
		{
			System.out.println(d.toString());
		}
	}
	public static ArrayList<Date> getNextExecutions(CronExpression cronExpression, int numExecutions) {
		
		ArrayList<Date> execs = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance();
		Date time = cal.getTime();
		int n = 0;
		while((n < numExecutions))// && (nextDate.before(finalFireTime)|| nextDate.equals(finalFireTime)))
		{
			time = cronExpression.getNextValidTimeAfter(time);
			if(time != null)
				execs.add(time);
			else return execs;
			n++;
		}
		return execs;
	}
}
