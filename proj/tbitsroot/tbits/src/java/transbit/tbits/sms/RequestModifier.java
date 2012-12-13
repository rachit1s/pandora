package transbit.tbits.sms;

import java.util.ArrayList;
import java.util.Hashtable;

import org.jfree.util.Log;

import transbit.tbits.domain.Request;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.DataType;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.RequestUser;
import transbit.tbits.exception.TBitsException;
import transbit.tbits.config.BusinessRule;
import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.APIUtil;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;

/**
 * User: yes
 * Date: Jun 5, 2007
 * Time: 5:59:28 PM
 */

/* This class take a Cloned request object compares it with the rules applicable to it
 if required it modifes cloned request object and sets send_sms true or false governed by the action of the matching rule
* */

public class RequestModifier{

	public static final TBitsLogger LOG = TBitsLogger
	.getLogger("transbit.tbits.sms");
	
    public String getTestXmlString(){
        StringBuilder sb = new StringBuilder();
                sb.append("<?xml version=\"1.0\" encoding=\"utf-8\" ?> ");
                sb.append("<Rule id=\"1\"> ");
                sb.append("	<expressions> ");
                sb.append("		<expression id=\"1\"> ");
                sb.append("			<name>subject</name> ");
                sb.append("			<op>EQ</op> ");
                sb.append("			<value>Hi</value> ");
                sb.append("		</expression> ");
                sb.append("			<expression id=\"2\"> ");
                sb.append("			<name>description</name> ");
                sb.append("			<op>EQ</op> ");
                sb.append("			<value>MD</value> ");
                sb.append("		</expression> ");
                sb.append("	</expressions> ");
                sb.append("	<actions> ");
                sb.append("		<action> ");
                sb.append("			<name>send_sms</name> ");
                sb.append("			<op>SET</op> ");
                sb.append("			<value>true</value> ");
                sb.append("		</action> ");
                sb.append("		<action> ");
                sb.append("			<name>sms_log</name> ");
                sb.append("			<op>APPEND</op> ");
                sb.append("			<value>request_id</value> ");
                sb.append("		</action> ");
                sb.append("	</actions> ");
                sb.append("</Rule>");
                String smsRuleXml = sb.toString();

        return smsRuleXml;
    }

   public boolean isRuleValid(Request request, Hashtable<String, Field> allFields, PostProcessXmlRule postProcessXmlConfig){

      boolean ruleValid = true;
      ArrayList<NotificationExpression> expressionList = postProcessXmlConfig.SmsExpressionList;
     
      for (NotificationExpression exp : expressionList) {
            String name = exp.getName();
            String ruleValue = exp.getValue();
            String op = exp.getOp();
            BusinessRule.Operator operator = BusinessRule.Operator.valueOf(op);
            Field field = allFields.get(name);
            if (!APIUtil.compareFieldValueAndRuleValue(field.getDataTypeId(),name,request.get(name),ruleValue, operator))
            {
                ruleValid = false;
                break;
            }
        }
   return ruleValid;
  }

   /**
    * Modifies the request based on rules.
    * @param request An object of request that has to be modified
    * @param postProcessXmlConfig The XML representation of Rule Configuration.
    * @return true, if it makes any change in the request otherwise false. 
    * @throws DatabaseException
    */
   @Deprecated
    public boolean modifyRequest(Request request,PostProcessXmlRule postProcessXmlConfig) throws DatabaseException {
               Hashtable<String, Field> allFields = Field.getFieldsTableBySystemId(request.getSystemId());
//        if(isRuleValid(request,allFields, postProcessXmlConfig))
//        {
//            ArrayList<NotificationAction> actionList = postProcessXmlConfig.SmsActionList;
//            for(NotificationAction act : actionList)
//            {
//                Hashtable<Field, RequestEx> exFields = request.getExtendedFields();
//                BusinessRule.Operator operator = BusinessRule.Operator.valueOf(act.getOp());
//                APIUtil.modify(act.getName(), act.getValue(), operator,
//                		allFields, exFields, request);
//		                request.setExtendedFields(exFields);
//            }
//         return true;
//        }
//
//        else return false;
               return false ;

    }

    public static void main(String[] args) throws DatabaseException {
//        RequestModifier requestModifier = new RequestModifier();
//        Request request = new Request();
//        request.setDescription("MD");
//        request.setSubject("Hi");
//        request.setSystemId(1);
//        RequestUser rootReqUser = new RequestUser();
//        rootReqUser.setSystemId(1);
//        rootReqUser.setUserId(1);
//        ArrayList<RequestUser> subs = new  ArrayList<RequestUser>();
//        subs.add(rootReqUser);
//        request.setSubscribers(subs);
//        
//        Hashtable<Field, RequestEx> exFields = new Hashtable<Field, RequestEx>();
//        Field f = Field.lookupBySystemIdAndFieldName(1, "send_sms");
//        RequestEx reqEx = new RequestEx();
//        reqEx.setFieldId(f.getFieldId());
//        reqEx.setSystemId(1);
//        reqEx.setRequestId(request.getRequestId());
//        reqEx.setBitValue(false);
//        exFields.put(f, reqEx);
//        try {
//            request.setExtendedFields(exFields);
//            PostProcessXmlRule xmlRule = PostProcessXmlRule.createFromXML(requestModifier.getTestXmlString());
//            requestModifier.modifyRequest(request, xmlRule);
//        } catch (DatabaseException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (InstantiationException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        } catch (TBitsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

    }
}
