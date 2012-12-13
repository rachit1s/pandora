package transbit.tbits.sms;

/**
 * Created by IntelliJ IDEA.
 * User: yes
 * Date: Jun 12, 2007
 * Time: 9:34:08 PM
 * To change this template use File | Settings | File Templates.
 */
/*
* Each object of this class represents a row of table containing info aboutr SMS logs
* */

public class SmsBaCount {
int ba;
int smsCount;

    public int getBa() {
        return ba;
    }

    public void setBa(int ba) {
        this.ba = ba;
    }

    public int getSmsCount() {
        return smsCount;
    }

    public void setSmsCount(int smsCount) {
        this.smsCount = smsCount;
    }
}
