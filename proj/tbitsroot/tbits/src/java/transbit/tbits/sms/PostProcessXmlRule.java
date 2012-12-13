package transbit.tbits.sms;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: yes
 * Date: Jun 5, 2007
 * Time: 6:48:33 PM
 * To change this template use File | Settings | File Templates.
 */

/*
* This class is responsible for getting xmlString and returning an object of
* PostProcessXmlRule(which contains all conditions & expressions for a given rule)
* */

public class PostProcessXmlRule {
ArrayList<NotificationExpression> SmsExpressionList;
ArrayList<NotificationAction> SmsActionList;

public PostProcessXmlRule(ArrayList<NotificationExpression> smsExpressionList, ArrayList<NotificationAction> smsActionList) {
        SmsExpressionList = smsExpressionList;
        SmsActionList = smsActionList;
    }
    public static PostProcessXmlRule createFromXML(String xmlString) throws InstantiationException {

           ArrayList<NotificationExpression> smsExpressionList = new ArrayList<NotificationExpression>();
           ArrayList<NotificationAction> smsActionList = new ArrayList<NotificationAction>();
           PostProcessXmlRule postProcessXmlRule =new PostProcessXmlRule(smsExpressionList,smsActionList);

           DocumentBuilderFactory dbfactory = DocumentBuilderFactory.newInstance();
           dbfactory.setIgnoringComments(true);
           try {
               DocumentBuilder builder = dbfactory. newDocumentBuilder();
               Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
               NodeList nodeList = document.getElementsByTagName("expression"); //list of all nodes having tag = expression
               for (int i = 0; i < nodeList.getLength(); i++) {
                   Node expression = nodeList.item(i);
                   NodeList expressionList = expression.getChildNodes();  //expressionList is list of all childnodes in expression
                   NotificationExpression notificationExpression = new NotificationExpression();

                   for (int j = 0; j < expressionList.getLength(); j++) {
                       notificationExpression.setExpressionId(j);

                       if (expressionList.item(j).getNodeName().equalsIgnoreCase("name")) {
                           notificationExpression.setName(expressionList.item(j).getTextContent());
                       }

                       if (expressionList.item(j).getNodeName().equalsIgnoreCase("op")) {
                           notificationExpression.setOp(expressionList.item(j).getTextContent());
                       }

                       if (expressionList.item(j).getNodeName().equalsIgnoreCase("value")) {
                           notificationExpression.setValue(expressionList.item(j).getTextContent());
                       }
                   }
                   smsExpressionList.add(notificationExpression);

               }

               NodeList nodeListAction = document.getElementsByTagName("action");

               for (int i = 0; i < nodeListAction.getLength(); i++) {
                   Node action = nodeListAction.item(i);       //collects nodes of action
                   NodeList actionList = action.getChildNodes(); //collects
                   NotificationAction smsAction = new NotificationAction();

                   for (int j = 0; j < actionList.getLength(); j++) {
                       if (actionList.item(j).getNodeName().equalsIgnoreCase("name")) {
                           smsAction.setName(actionList.item(j).getTextContent());
                       }

                       if (actionList.item(j).getNodeName().equalsIgnoreCase("op")) {
                           smsAction.setOp(actionList.item(j).getTextContent());
                       }

                       if (actionList.item(j).getNodeName().equalsIgnoreCase("value")) {
                           smsAction.setValue(actionList.item(j).getTextContent());
                       }

                   }
                   smsActionList.add(smsAction);
               }

           } catch (Exception e) {
               throw new InstantiationException("Invalid XML");
           }
           //System.out.println("postProcessXmlRule = " + postProcessXmlRule);
           return postProcessXmlRule;
       }

}
