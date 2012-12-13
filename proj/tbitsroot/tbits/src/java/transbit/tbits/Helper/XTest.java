/*
 * Copyright (c) 2005 Transbit Technologies Pvt. Ltd. All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Transbit Technologies Pvt. Ltd. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Transbit Technologies Pvt. Ltd.
 */



package transbit.tbits.Helper;

//~--- non-JDK imports --------------------------------------------------------

import transbit.tbits.common.TBitsLogger;
import transbit.tbits.config.BAConfig;
import transbit.tbits.config.WebConfig;
import transbit.tbits.domain.BAUser;
import transbit.tbits.domain.BusinessArea;
import transbit.tbits.domain.User;

import static transbit.tbits.Helper.TBitsConstants.PKG_UTIL;

//~--- JDK imports ------------------------------------------------------------

import java.util.ArrayList;

//~--- classes ----------------------------------------------------------------

public class XTest {
    public static final TBitsLogger LOG = TBitsLogger.getLogger(PKG_UTIL);

    //~--- methods ------------------------------------------------------------

    /**
     * @param args
     */
    public static int main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage:  XTest <Start SystemId> <End SystemId>");
            return 1;
        }

        int start = Integer.parseInt(args[0]);
        int end   = Integer.parseInt(args[1]);

        for (int i = start; i <= end; i++) {
            run(i);
        }

        return 0;
    }

    public static void run(int aSystemId) {
        try {
            BusinessArea ba = BusinessArea.lookupBySystemId(aSystemId);

            if (ba == null) {
                LOG.info("Invalid BusinessArea: " + aSystemId);

                return;
            }

            String          sysPrefix = ba.getSystemPrefix();
            ArrayList<User> list      = BAUser.getBusinessAreaUsers(aSystemId);

            if ((list == null) || (list.size() == 0)) {
                LOG.info("No Users in this BA: " + sysPrefix);

                return;
            }

            for (User user : list) {
                WebConfig webConfig = user.getWebConfigObject();
                BAConfig  baConfig  = webConfig.getBAConfig(sysPrefix);

                baConfig.setEnableVE(true);
                webConfig.setBAConfig(sysPrefix, baConfig);
                user.setWebConfig(webConfig.xmlSerialize());
                User.update(user);
                LOG.info("Enabled read/unread option for " + user.getUserLogin());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
