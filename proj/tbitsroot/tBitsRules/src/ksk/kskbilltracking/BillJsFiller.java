package kskbilltracking;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import transbit.tbits.ExtUI.IUpdateRequestFooterSlotFiller;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.User;
import static kskbilltracking.BillHelper.*;
import static kskbilltracking.BillProperties.*;
import static kskbilltracking.BillConstants.*;

public class BillJsFiller implements IUpdateRequestFooterSlotFiller {

	@Override
	public double getUpdateRequestFooterSlotFillerOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static String jsBuilder(ArrayList<String> fieldList){
		String template="document.getElementById('<%=field%>').disabled=true;\n";
		StringBuffer sb = new StringBuffer();
		for(String st:fieldList){
			sb.append(template.replaceAll("<%=field%>",st));
		}

		return sb.toString();

	}

	@Override
	public String getUpdateRequestFooterHtml(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, BusinessArea ba,
			Request oldRequest, User user) {
		
		if( null == ba || null == ba.getSystemPrefix() || ! ba.getSystemPrefix().equalsIgnoreCase(Bill_sysprefix))
			return "" ;

		try{

			int processId=getProcessId(oldRequest);
			//  int stepId=getStepId(oldRequest);


			StringBuffer disableJs=new StringBuffer();
			disableJs.append("<script type='text/javascript'>\n");

			Hashtable<String,Hashtable<String,String>>allStepsHash=getActionTableByAllSteps(processId);
			Enumeration<String>stepper=allStepsHash.keys();
			ArrayList<String>disableList=new ArrayList<String>();
			while(stepper.hasMoreElements()){
				String step=stepper.nextElement();
				Hashtable<String,String>stepHash=allStepsHash.get(step);
				if(stepHash.get(Db_step_Duration).equals("0")){
					if(step.equals("1")){
						disableList.add(stepHash.get(Db_step_Dep_Acknowledge_Date));
						disableList.add(stepHash.get(Db_step_Dep_Receipt_Date));
					}

					else{
						disableList.add(stepHash.get(Db_step_Dep_Acknowledge_Date));
						disableList.add(stepHash.get(Db_step_Dep_Receipt_Date));
						disableList.add(stepHash.get(Db_step_Decision));

					}
				}

			}
			disableJs.append(jsBuilder(disableList));
			disableJs.append("</script>");
			return disableJs.toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}

		return null;
	}

	
}
