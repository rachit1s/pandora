package transbit.tbits.sms;

/**
 * Created by IntelliJ IDEA.
 * User: yes
 * Date: Jun 1, 2007
 * Time: 10:16:18 PM
 * To change this template use File | Settings | File Templates.
 */

/*
* An Object of this Class represents a condition in a rule
* */

public class NotificationExpression {
int expressionId;
String name;
String op;
String value;

    public int getExpressionId() {
        return expressionId;
    }

    public void setExpressionId(int expressionId) {
        this.expressionId = expressionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOp() {
        return op;
    }

    public void setOp(String op) {
        this.op = op;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
