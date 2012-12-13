package transbit.tbits.scheduler.sap.request;

import java.io.Serializable;
import java.util.Map;

/**
 * @author: Mukesh Sharma
 * Date: 22/8/12
 * Time: 11:26 AM
 */
public class PropertyObject implements Serializable {

    static final long serialVersionUID = -7588980448693010499L;

    Map<String, String> propertyMap;
    
    String propertyFilePath;
    
    String sapObjectFilePath;

    public void setPropertyFilePath(String propertyFilePath) {
        this.propertyFilePath = propertyFilePath;
    }

    public void setSapObjectFilePath(String sapObjectFilePath) {
        this.sapObjectFilePath = sapObjectFilePath;
    }

    public String getSapObjectFilePath() {
        return sapObjectFilePath;
    }

    public String getPropertyFilePath() {
        return propertyFilePath;
    }

    public PropertyObject(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }

    public Map<String, String> getPropertyMap() {
        return propertyMap;
    }

    public void setPropertyMap(Map<String, String> propertyMap) {
        this.propertyMap = propertyMap;
    }

}
