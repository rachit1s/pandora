package transbit.tbits.scheduler.sap.request;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.scheduler.sap.util.Constants;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author: Mukesh Sharma
 * Date: 21/8/12
 * Time: 5:35 PM
 */
public class SapObject implements Serializable {

    static final long serialVersionUID = -7588980448693010399L;

    boolean rfc;

    boolean asBuilt;

    //mappingData contains key and value to be inserted
    Map<String, String> mappingData;

    //rfcFiles contains absolute file Path and display name.
    Map<String, String> rfcFiles;

    //asBuiltFiles contains absolute file Path and display name.
    Map<String, String> asBuiltFiles;

    public SapObject() {
    }

    public void setRfc(boolean rfc) {
        this.rfc = rfc;
    }

    public void setAsBuilt(boolean asBuilt) {
        this.asBuilt = asBuilt;
    }

    public boolean isRfc() {
        return rfc;
    }

    public boolean isAsBuilt() {
        return asBuilt;
    }

    public void setMappingData(Map<String, String> mappingData) {
        this.mappingData = mappingData;
    }

    public void setRfcFiles(Map<String, String> rfcFiles) {
        this.rfcFiles = rfcFiles;
    }

    public void setAsBuiltFiles(Map<String, String> asBuiltFiles) {
        this.asBuiltFiles = asBuiltFiles;
    }

    public Map<String, String> getMappingData() {
        return mappingData;
    }

    public Map<String, String> getAsBuiltFiles() {
        return asBuiltFiles;
    }

    public Map<String, String> getRfcFiles() {
        return rfcFiles;
    }
}
