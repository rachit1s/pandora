package transbit.tbits.scheduler.sap.util;

import transbit.tbits.common.Configuration;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.scheduler.sap.request.PropertyObject;
import transbit.tbits.scheduler.sap.request.SapObject;

import java.io.*;
import java.util.List;

/**
 * @author: Mukesh Sharma
 * Date: 22/8/12
 * Time: 11:27 AM
 */
public class WriteToFile {
    public static final TBitsLogger LOG = TBitsLogger.getLogger("WriteTOFile");

    public static boolean writeSapObjects(List<SapObject> sapObjectList) {
        try {
            //use buffering

            String tmpPath = Configuration.findAbsolutePath("tmp");
//            propertyObject.setSapObjectFilePath(tmpPath + Constants.SAP_OBJECT_OUTPUT_FILE);
            OutputStream file = new FileOutputStream(tmpPath + Constants.SAP_OBJECT_OUTPUT_FILE);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            try {
                output.writeObject(sapObjectList);
                output.flush();
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            LOG.debug("Cannot perform output.", ex);
            return false;
        }

        return true;
    }

    public static boolean writePropertyObject(PropertyObject propertyObject) {
        try {
            //use buffering

            String tmpPath = Configuration.findAbsolutePath("tmp");
            OutputStream file = new FileOutputStream(tmpPath + Constants.PROP_OBJECT_OUTPUT_FILE);
            OutputStream buffer = new BufferedOutputStream(file);
            ObjectOutput output = new ObjectOutputStream(buffer);
            try {
                output.writeObject(propertyObject);
            } finally {
                output.close();
            }
        } catch (IOException ex) {
            LOG.debug("Cannot perform output.", ex);
            return false;
        }

        return true;
    }


    public static List<SapObject> readSapObjects() {
        List<SapObject> recoveredSapObjects = null;
        try {
            //use buffering
            InputStream file = new FileInputStream(Constants.SAP_OBJECT_OUTPUT_FILE);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                //deserialize the List
                recoveredSapObjects = (List<SapObject>) input.readObject();
            } finally {
                input.close();
            }
        } catch (ClassNotFoundException ex) {
            LOG.debug("Cannot perform input. Class not found.", ex);
        } catch (IOException ex) {
            LOG.debug("Cannot perform input.", ex);
        }
        return recoveredSapObjects;
    }

    public static PropertyObject readPropertyObjects() {
        PropertyObject recoveredPropertyObjects = null;
        try {
            //use buffering
            InputStream file = new FileInputStream(Constants.PROP_OBJECT_OUTPUT_FILE);
            InputStream buffer = new BufferedInputStream(file);
            ObjectInput input = new ObjectInputStream(buffer);
            try {
                //deserialize the List
                recoveredPropertyObjects = (PropertyObject) input.readObject();

            } finally {
                input.close();
            }
        } catch (ClassNotFoundException ex) {
            LOG.debug("Cannot perform input. Class not found.", ex);
        } catch (IOException ex) {
            LOG.debug("Cannot perform input.", ex);
        }
        return recoveredPropertyObjects;
    }
}
