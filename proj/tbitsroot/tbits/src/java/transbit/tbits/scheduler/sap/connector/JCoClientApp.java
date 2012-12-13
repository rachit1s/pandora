package transbit.tbits.scheduler.sap.connector;

import transbit.tbits.scheduler.sap.util.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

/**
 * @author: Mukesh Sharma
 * Date: 11/7/12
 * Time: 2:11 PM
 */
public class JCoClientApp {
//    static final String DESTINATION_NAME = "DESTINATION_WITHOUT_POOL";

    public static void createDestinationDataFile(Properties connectionProperties) {

        File destCfg = new File("/tmp/" + Constants.DESTINATION_NAME + ".jcoDestination");
        try {
            FileOutputStream fos = new FileOutputStream(destCfg, false);
            connectionProperties.store(fos, "for tests only !");
            fos.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to create the destination files", e);
        }
    }
}
