package transbit.tbits.sms;

/**
 * Created by IntelliJ IDEA.
 * User: yes
 * Date: Jun 6, 2007
 * Time: 4:32:00 AM
 * To change this template use File | Settings | File Templates.
 */
/*
* This class corresponds to a row of table containg all the information associated with the rule
* */
public class RuleObject {
int sysId;
int ruleId;
int userId;
int priority;
String xmlString;
String description;
boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public RuleObject(int sysId, int ruleId, int userId, int priority, String xmlString, String description, boolean enabled) {
        this.sysId = sysId;
        this.ruleId = ruleId;
        this.userId = userId;
        this.priority = priority;
        this.xmlString = xmlString;
        this.description = description;
        this.enabled = enabled;
    }

    public int getSysId() {
        return sysId;
    }

    public void setSysId(int sysId) {
        this.sysId = sysId;
    }

    public int getRuleId() {
        return ruleId;
    }

    public void setRuleId(int ruleId) {
        this.ruleId = ruleId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public String getXmlString() {
        return xmlString;
    }

    public void setXmlString(String xmlString) {
        this.xmlString = xmlString;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
