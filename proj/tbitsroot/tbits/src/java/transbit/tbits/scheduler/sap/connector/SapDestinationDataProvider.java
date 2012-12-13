package transbit.tbits.scheduler.sap.connector;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;

import java.util.Properties;

/**
 * @author: Mukesh Sharma
 * Date: 9/8/12
 * Time: 11:21 AM
 */
public class SapDestinationDataProvider implements DestinationDataProvider {

    private DestinationDataEventListener eL;
    private Properties ABAP_AS_properties;
    private String destinationName;

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public Properties getDestinationProperties(String destinationName) {

        if (destinationName.equals(this.destinationName) &&
                ABAP_AS_properties!=null)
            return ABAP_AS_properties;

        return null;
    }

    public boolean supportsEvents() {
        return true;
    }

    public void changeProperties(Properties properties) {
        if(properties==null) {
            eL.deleted(this.destinationName);
            ABAP_AS_properties = null;
        }
        else {
            if (ABAP_AS_properties!=null &&
                    !ABAP_AS_properties.equals(properties))
                eL.updated(this.destinationName);
            ABAP_AS_properties = properties;
        }
    }

    public void setDestinationDataEventListener(
            DestinationDataEventListener eventListener) {
        this.eL = eventListener;
    }

}