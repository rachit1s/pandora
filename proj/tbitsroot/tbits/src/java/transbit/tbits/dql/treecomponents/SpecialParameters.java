package transbit.tbits.dql.treecomponents;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

public class SpecialParameters{

	// DQL DATETIME values
	private static final long MIN_INTERVAL = (60*1000);
	private static final long DAY_INTERVAL = (24*60*MIN_INTERVAL);
	public static final String TODAY_DATETIME = "today";
	public static final String YESTERDAY_DATETIME = "yesterday";
	public static final String TOMORROW_DATETIME = "tomorrow";
	public static final String LAST_WEEK_DATETIME = "lastweek";
	public static final String LAST_MONTH_DATETIME = "lastmonth";
	public static final String LAST_YEAR_DATETIME = "lastyear";

	public static final String MINS_DATETIME = "m";
	public static final String HOURS_DATETIME = "h";
	public static final String DATE_DATETIME = "d";
	public static final String MONTH_DATETIME = "M";
	public static final String YEAR_DATETIME = "y";
	
	private static HashMap<String, Date> spMap;
	//private static SpecialParameters specialParams;
	
	public static SpecialParameters getInstance(){
		/*if(specialParams == null)
			specialParams = new SpecialParameters();*/
		return new SpecialParameters();
	}
	
	public HashMap<String, Date> getSpMap(){
		return spMap;
	}
	
	private SpecialParameters(){

		spMap = new HashMap<String, Date>();
		
		// DATETIME parameters
		Date today = new Date();
		today.setHours(0);
		today.setMinutes(0);
		today.setSeconds(0);
		spMap.put(TODAY_DATETIME, today);
		Date yesterday = new Date(today.getTime() - DAY_INTERVAL);
		spMap.put(YESTERDAY_DATETIME, yesterday);
		Date tomorrow = new Date(today.getTime() + DAY_INTERVAL);
		spMap.put(TOMORROW_DATETIME, tomorrow);
		Date lastWeek = new Date(today.getTime() - (7*DAY_INTERVAL));
		spMap.put(LAST_WEEK_DATETIME, lastWeek);
		Calendar cal = Calendar.getInstance();
		cal.setTime(today);
		if(cal.get(Calendar.MONTH) == 0){
			cal.set(Calendar.MONTH, Calendar.DECEMBER);
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
		}
		else
			cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) - 1);
		spMap.put(LAST_MONTH_DATETIME, cal.getTime());
		cal.setTime(today);
		cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)-1);
		spMap.put(LAST_YEAR_DATETIME, cal.getTime());
	}

	public Parameter getSpecialParam(String paramString) throws Exception {
		
		String specialString = paramString;
		String specialParam = null;
		boolean add = false;;
		boolean sub = false;
		int addIndex = paramString.indexOf("+", 0);
		int subIndex = -1;
		if(addIndex < 0){
			subIndex = paramString.indexOf("-", 0);
			if(subIndex >= 0){
				sub = true;
				specialString = paramString.substring(0, subIndex);
				specialParam = paramString.substring(subIndex+1);
			}
		}
		else{
			add = true;
			specialString = paramString.substring(0, addIndex);
			specialParam = paramString.substring(addIndex+1);
		}
		
		Parameter toRet = new Parameter();
		toRet.type = DqlConstants.ParamType.DATETIME;
		Date retDate = null;
		if(!specialString.equals("")){
			if(spMap.containsKey(specialString))
				retDate = spMap.get(specialString);
			else{
				StringTokenizer st = new StringTokenizer(specialString, DqlConstants.DATE_DELIM);
				try{
					int date = Integer.parseInt(st.nextToken().trim());
					int month = Integer.parseInt(st.nextToken().trim());
					int year = Integer.parseInt(st.nextToken().trim());
					
					String dateStr = year+"-"+month+"-"+date;
					
					SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
					retDate = formatter.parse(dateStr);
				}
				catch(NoSuchElementException ne){
					retDate = null;
				}
			    catch (ParseException e){
			    	retDate = null;
			    }
			    catch (NumberFormatException nfe){
			    	retDate = null;
			    }
			}
			if(retDate == null)
				return null;
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(retDate);
		cal.setTimeZone(TimeZone.getTimeZone("GMT"));
		HashMap<String, Integer> params = getParams(specialParam);
		if(add){
			retDate.setTime(retDate.getTime() + (params.get(MINS_DATETIME)*MIN_INTERVAL) + 
					(params.get(HOURS_DATETIME)*60*MIN_INTERVAL) + (params.get(DATE_DATETIME)*DAY_INTERVAL));
			cal.setTime(retDate);
			int M = cal.get(Calendar.MONTH) + params.get(MONTH_DATETIME);
			cal.set(Calendar.MONTH, M%12);
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + (M/12) + params.get(YEAR_DATETIME));
		}
		else if(sub){
			retDate.setTime(retDate.getTime() - (params.get(MINS_DATETIME)*MIN_INTERVAL) -
					(params.get(HOURS_DATETIME)*60*MIN_INTERVAL) - (params.get(DATE_DATETIME)*DAY_INTERVAL));
			cal.setTime(retDate);
			int M = cal.get(Calendar.MONTH) - params.get(MONTH_DATETIME);
			cal.set(Calendar.MONTH, (M<0)?12+M:M);
			cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - ((M<0)?(1+(Math.abs(M)/12)):0) - params.get(YEAR_DATETIME));
		}
		toRet.param = cal.get(Calendar.YEAR) + "-" + (cal.get(Calendar.MONTH)+1) + "-" + cal.get(Calendar.DATE) + " " 
					+ cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + ":" + cal.get(Calendar.SECOND) + "." + cal.get(Calendar.MILLISECOND);
		
		return toRet;
	}
	
	private HashMap<String, Integer> getParams(String p){
		
		HashMap<String, Integer> params = new HashMap<String, Integer>();
		params.put(MINS_DATETIME, 0);
		params.put(HOURS_DATETIME, 0);
		params.put(DATE_DATETIME, 0);
		params.put(MONTH_DATETIME, 0);
		params.put(YEAR_DATETIME, 0);
		
		if(p == null)
			return params;
		
		ArrayList<Integer> temp = new ArrayList<Integer>();
		if(p.indexOf(MINS_DATETIME, 0) > 0)
			temp.add(p.indexOf(MINS_DATETIME, 0));
		if(p.indexOf(HOURS_DATETIME, 0) > 0)
			temp.add(p.indexOf(HOURS_DATETIME, 0));
		if(p.indexOf(DATE_DATETIME, 0) > 0)
			temp.add(p.indexOf(DATE_DATETIME, 0));
		if(p.indexOf(MONTH_DATETIME, 0) > 0)
			temp.add(p.indexOf(MONTH_DATETIME, 0));
		if(p.indexOf(YEAR_DATETIME, 0) > 0)
			temp.add(p.indexOf(YEAR_DATETIME, 0));
		
		Collections.sort(temp);
		String parsed = p;
		
		for(int i : temp){
			if(i == p.indexOf(MINS_DATETIME, 0)){
				params.put(MINS_DATETIME, Integer.parseInt(parsed.substring(0, parsed.indexOf(MINS_DATETIME, 0))));
				parsed = parsed.substring(parsed.indexOf(MINS_DATETIME, 0)+1);
			}
			else if(i == p.indexOf(HOURS_DATETIME, 0)){
				params.put(HOURS_DATETIME, Integer.parseInt(parsed.substring(0, parsed.indexOf(HOURS_DATETIME, 0))));
				parsed = parsed.substring(parsed.indexOf(HOURS_DATETIME, 0)+1);
			}
			else if(i == p.indexOf(DATE_DATETIME, 0)){
				params.put(DATE_DATETIME, Integer.parseInt(parsed.substring(0, parsed.indexOf(DATE_DATETIME, 0))));
				parsed = parsed.substring(parsed.indexOf(DATE_DATETIME, 0)+1);
			}
			else if(i == p.indexOf(MONTH_DATETIME, 0)){
				params.put(MONTH_DATETIME, Integer.parseInt(parsed.substring(0, parsed.indexOf(MONTH_DATETIME, 0))));
				parsed = parsed.substring(parsed.indexOf(MONTH_DATETIME, 0)+1);
			}
			else if(i == p.indexOf(YEAR_DATETIME, 0)){
				params.put(YEAR_DATETIME, Integer.parseInt(parsed.substring(0, parsed.indexOf(YEAR_DATETIME, 0))));
				parsed = parsed.substring(parsed.indexOf(YEAR_DATETIME, 0)+1);
			}
		}
		
		return params;
	}
	
}
