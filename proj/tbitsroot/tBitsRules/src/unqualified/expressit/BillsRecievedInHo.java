package expressit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Hashtable;
import java.util.Properties;

import transbit.tbits.Helper.TBitsConstants;
import transbit.tbits.api.IRule;
import transbit.tbits.api.RuleResult;
import transbit.tbits.common.DatabaseException;
import transbit.tbits.common.TBitsLogger;
import transbit.tbits.common.Timestamp;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.CalenderUtils;
import transbit.tbits.domain.Field;
import transbit.tbits.domain.Request;
import transbit.tbits.domain.RequestEx;
import transbit.tbits.domain.User;

public class BillsRecievedInHo implements IRule {
	 private static final String BALANCE_VERIFIED = "BalanceVerified";
	private static final int DUE_DATE_LAG = 5;
	private static final String BILLS_RECEIVED_IN_HO_STATUS = "BillsreceivedinHO";
	private static final String IMPREST_SYS_PREFIX = "imprest.sys_prefix";
	// Application logger.
    public static final TBitsLogger LOG = TBitsLogger.getLogger(TBitsConstants.PKG_EXTERNAL);
    
    public static String getConfiguredBusinessArea()
    {
    	URL url = BillsRecievedInHo.class.getResource("app.properties");
		String file = url.getFile();
		File f = new File(file);
		if (f.exists()) {
			Properties props = new Properties();
			try {
				props.load(new FileInputStream(f));
				String imprestBaPrefix = props.getProperty(IMPREST_SYS_PREFIX);
				return imprestBaPrefix;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			LOG.error("BillsRecievedInHo: The " + f.getAbsolutePath()
					+ " file is mising. Please check is it exist.");
		}
		return null;
    }
	public RuleResult execute(BusinessArea ba, Request oldRequest,
			Request currentRequest, int Source, User user,
			Hashtable<Field, RequestEx> extendedFields, boolean isAddRequest,
			String attachments) {
		String imprestBaPrefix = getConfiguredBusinessArea();
		if (imprestBaPrefix != null) {
			if (ba.getSystemPrefix().equalsIgnoreCase(imprestBaPrefix)) {
				if (currentRequest.getStatusId().getName().equals(
						BILLS_RECEIVED_IN_HO_STATUS)
						&& ((oldRequest == null) || !oldRequest.getStatusId()
								.getName().equalsIgnoreCase(
										BILLS_RECEIVED_IN_HO_STATUS))) {
					Date dueDate = CalenderUtils.slideDate(new Date(),
							DUE_DATE_LAG);
					currentRequest.setDueDate(new Timestamp(dueDate.getTime()));
					return new RuleResult(true,
							"BillsRecievedInHo: Updating the due date.", true);
				} else if (currentRequest.getStatusId().getName()
						.equalsIgnoreCase("Closed")) {
					Field bvF = null;
					try {
						bvF = Field.lookupBySystemIdAndFieldName(ba
								.getSystemId(), BALANCE_VERIFIED);
					} catch (DatabaseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (bvF != null) {
						RequestEx bvRE = extendedFields.get(bvF);
						if (bvRE != null) {
							boolean balanceVerified = bvRE.getBitValue();
							if (!balanceVerified) {
								return new RuleResult(
										false,
										"While closing the request you should also specify the Balance Valified.");
							}
						} else {
							LOG
									.error("BillsRecievedInHo: The request ex for the field '"
											+ BALANCE_VERIFIED
											+ "' is not found.");
						}
					} else {
						LOG.error("BillsRecievedInHo: The field "
								+ BALANCE_VERIFIED + " is not found.");
					}
				}
			} else {
				LOG
						.info("BillsRecievedInHo: Skipping the rule BillsRecievedInHo for ba '"
								+ ba.getSystemPrefix()
								+ "' as it should only run for '"
								+ imprestBaPrefix + "'");
			}
		} else {
			LOG.error("BillsRecievedInHo: Unable to find property '"
					+ IMPREST_SYS_PREFIX + "'");
		}
		return new RuleResult(true, "", true);
	}

	public String getName() {
		// TODO Auto-generated method stub
		return "Changes the due date if status is BillsRecievedInHo";
	}

	public double getSequence() {
		// TODO Auto-generated method stub
		return 0;
	}
}
