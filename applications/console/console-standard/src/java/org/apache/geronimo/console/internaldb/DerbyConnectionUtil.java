/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.console.internaldb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.management.ObjectName;
import javax.sql.DataSource;

import org.apache.geronimo.console.GeronimoVersion;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * A static class to handle retreiving connections. This class is built to
 * handle lookups to the SystemDatabase as a special case. If a connection is
 * requested for the SystemDatabase this class gets a DataSource from an admin
 * object registered in the geronimo kernel otherwise the DataSource is looked
 * up via JNDI.
 */
public class DerbyConnectionUtil {

    public static final String CREATE_DB_PROP = ";create=true";

    public static final String SHUTDOWN_DB_PROP = ";shutdown=true";

    private static final int RDBMS_DERBY = 1;

    private static final int RDBMS_MSSQL = 2;

    private static final String SYSTEM_DB = "SYSTEMDATABASE";

    private static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String PROTOCOL = "jdbc:derby:";

    private static final String EMPTY_PROPS = "";

    private static final ObjectName SYSTEM_DATASOURCE_NAME = JMXUtil
            .getObjectName("geronimo.server:J2EEApplication=null,J2EEServer=geronimo,JCAResource=geronimo/system-database/"+GeronimoVersion.GERONIMO_VERSION+"/car,j2eeType=JCAManagedConnectionFactory,name=SystemDatasource");

    /**
     * Get database connection.
     *
     * @param dbName
     * @return
     * @throws SQLException
     */
    private static Connection getConnection(String dbName, String properties,
            String protocol, String driver) throws SQLException {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            // Problem loading driver class
            return null;
        }
        // If we are looking for the SystemDatabase get it from the kernel
        // because it is not binded to our JNDI Context.
        if (SYSTEM_DB.equalsIgnoreCase(dbName)) {
            return getSystemDBConnection();
        } else {
            return DriverManager.getConnection(protocol + dbName + properties);
        }
    }

    /**
     * Get a connection to derby.
     *
     * @param dbName
     *            the name of the database to connect to.
     * @param properties
     *            the properties to pass to the connection string.
     * @return
     */
    public static Connection getDerbyConnection(String dbName, String properties)
            throws SQLException {
        return getConnection(dbName, properties, PROTOCOL, DERBY_DRIVER);
    }

    public static Connection getDerbyConnection(String dbName)
            throws SQLException {
        return getDerbyConnection(dbName, EMPTY_PROPS);
    }

    /**
     * Get a connection to the SystemDatabase.
     *
     * @return
     * @throws SQLException
     */
    public static Connection getSystemDBConnection() throws SQLException {
        DataSource ds = null;
        try {
            ds = getDataSource(SYSTEM_DB);
            return ds.getConnection();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Get the datasource if dbName is == SYSTEM_DB, otherwise returns null.
     *
     * @param dbName
     * @return
     */
    public static DataSource getDataSource(String dbName) {
        try {
            if (SYSTEM_DB.equalsIgnoreCase(dbName)) {
                return (DataSource) KernelRegistry.getSingleKernel().invoke(
                        SYSTEM_DATASOURCE_NAME, "$getResource");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

}
