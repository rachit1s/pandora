package com.tbitsglobal.izpack;
/*
 * File : JDBCConnectionDataValidator.java.java
 * Date : Aug 13, 2010
 * 
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 The KiWi Project. All rights reserved.
 * http://www.kiwi-project.eu
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  KiWi designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 */


import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import com.izforge.izpack.installer.AutomatedInstallData;
import com.izforge.izpack.installer.DataValidator;


/**
 * Used to to prove if the JDBC connection with a given database
 * can be done.
 * 
 * @author mihai, giris
 * @version 0.9
 * @since 0.9
 */
public final class DBConfigValidator implements DataValidator {

    private final String HOST = "DB_SERVER_NAME";

//    private final String PORT = "kiwi.database.port";

    private final String DB_NAME = "DB_NAME";

    private final String DB_USER = "DB_USERNAME";

    private final String DB_USER_PASSWORD = "DB_PASSWORD";
    private final String SKIP_DB_VALIDATION = "skip.db.validation";
    private final StringBuilder errorMessage;

    /**
     * 
     */
    public DBConfigValidator() {
        errorMessage = new StringBuilder();
    }

    /*
     * (non-Javadoc)
     * @see
     * com.izforge.izpack.installer.DataValidator#getDefaultAnswer
     * ()
     */
    public boolean getDefaultAnswer() {
        return false;
    }

    /*
     * (non-Javadoc)
     * @see
     * com.izforge.izpack.installer.DataValidator#getErrorMessageId
     * ()
     */
    public String getErrorMessageId() {
        return errorMessage.toString();
    }

    /*
     * (non-Javadoc)
     * @see
     * com.izforge.izpack.installer.DataValidator#getWarningMessageId
     * ()
     */
    public String getWarningMessageId() {
        return "";
    }

    /*
     * (non-Javadoc)
     * @see
     * com.izforge.izpack.installer.DataValidator#validateData
     * (com.izforge.izpack.installer.AutomatedInstallData)
     */
    public Status validateData(AutomatedInstallData data) {
        final Properties variables = data.getVariables();
        final String dbHost = variables.getProperty(HOST);
        final String dbName = variables.getProperty(DB_NAME);
        final String user = variables.getProperty(DB_USER);
        final String password = variables.getProperty(DB_USER_PASSWORD);
        final String skipCheck = variables.getProperty(SKIP_DB_VALIDATION);
        if(skipCheck.equalsIgnoreCase("true"))
        {
            System.out.println("Skipping db check");
            return Status.OK;
        }
       
        final String driver = "net.sourceforge.jtds.jdbc.Driver";
        final String url = "jdbc:jtds:sqlserver://"+ dbHost + "/" + dbName;

		buildErrorMessage(url);

         Status result = Status.OK;
		try
		{
			tryConnectTo(url, driver, user, password);
		}
		catch(Exception e)
		{
			errorMessage.append("Reason: " + e.getMessage());
			e.printStackTrace();
			result = Status.ERROR;
		}
        return result;
    }

	private void tryConnectTo(String url,String driver,String user,String password) throws SQLException, ClassNotFoundException
	{
		Class.forName(driver);
		DriverManager.getConnection(url,user, password);
	}
	
    private void buildErrorMessage(String url) {
        clearErrorMessage();

        errorMessage.append("The Database ");
        errorMessage.append(url);
        errorMessage.append(" can not be connected.");
    }

    private void clearErrorMessage() {
        if (!errorMessage.toString().isEmpty()) {
            errorMessage.delete(0, errorMessage.length());
        }
    }
}