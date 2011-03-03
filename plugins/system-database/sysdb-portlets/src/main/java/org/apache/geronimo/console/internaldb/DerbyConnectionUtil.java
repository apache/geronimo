/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.connector.outbound.GenericConnectionManager;
import org.apache.geronimo.console.util.KernelManagementHelper;
import org.apache.geronimo.console.util.ManagementHelper;
import org.apache.geronimo.derby.DerbySystemGBean;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;

/**
 * A static class to handle retreiving connections. This class is built to
 * handle lookups to the SystemDatabase as a special case. If a connection is
 * requested for the SystemDatabase this class gets a DataSource from an admin
 * object registered in the geronimo kernel otherwise the DataSource is looked
 * up via JNDI.
 *
 * @version $Rev$ $Date$
 */
public class DerbyConnectionUtil {

    private static final Logger log = LoggerFactory.getLogger(DerbyConnectionUtil.class);

	public static final String CREATE_DB_PROP = ";create=true";

    public static final String SHUTDOWN_DB_PROP = ";shutdown=true";

    private static final int RDBMS_DERBY = 1;

    private static final int RDBMS_MSSQL = 2;

    private static final String SYSTEM_DB = "SYSTEMDATABASE";

    private static final String DERBY_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";

    private static final String PROTOCOL = "jdbc:derby:";

    private static final String EMPTY_PROPS = "";
    
    private static AbstractName SYSTEM_DATASOURCE_NAME = null;
    
    static {
        try {
            log.debug("Looking up system datasource name...");

            // cache the name for the system data source
            AbstractNameQuery query = new AbstractNameQuery(GenericConnectionManager.class.getName());
            Set<AbstractName> names = KernelRegistry.getSingleKernel().listGBeans(query);
            for (AbstractName name : names) {
                String nameProperty = name.getNameProperty("name");
                if ("SystemDatasource".equals(nameProperty)) {
                    SYSTEM_DATASOURCE_NAME = name;
                    log.debug("Using system datasource name: " + SYSTEM_DATASOURCE_NAME);
                }
            }
            
            if (SYSTEM_DATASOURCE_NAME == null) {
                log.warn("Failed to lookup system datasource name");
            }
        }
        catch (Throwable t) {
            //
            // HACK: Log any errors which occur when this is loading...
            //       the system is not logging the full detail, which it should
            //       but for now lets show the details here
            //
            log.error("Failed to initialize", t);
            throw new Error(t);
        }
    }
    
    private static String derbyHome = null;
    
    /**
     * Get the Derby home directory path.
     */
    public static String getDerbyHome() {
        if (derbyHome == null) {
            try {
                derbyHome = (String)KernelRegistry.getSingleKernel().getAttribute(DerbySystemGBean.class, "derbyHome");
            }
            catch (Exception e) {
                throw new RuntimeException("Failed to query derbyHome", e);
            }
        }
        return derbyHome;
    }
    
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
        	log.error("Problem loading driver class", e);
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
     * @return connection
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
            ds = getDataSourceForDataBaseName(SYSTEM_DB);
            return ds.getConnection();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
    }

    /**
     * Get a connaction to a named datasource
     * 
     * @param dbName
     * @return
     * @throws SQLException 
     */
	public static Connection getDataSourceConnection(String dataSourceName) throws SQLException{
        try {
        	return getDataSource(dataSourceName).getConnection();
        } catch (Exception e) {
            throw new SQLException(e.getMessage());
        }
	}

    /**
     * Get the datasource if dbName is == SYSTEM_DB, otherwise finds the datasource among JCAManagedConnectionFactories, otherwise returns null.
     *
     * @param dbName
     * @return datasource
     */
    public static DataSource getDataSourceForDataBaseName(String dbName) {
        try {
            if (SYSTEM_DATASOURCE_NAME!=null && SYSTEM_DB.equalsIgnoreCase(dbName)) {
            	return (DataSource) KernelRegistry.getSingleKernel().invoke(
            			SYSTEM_DATASOURCE_NAME, "$getResource");
            }
        } catch (Exception e) {
        	log.error("Problem getting datasource " + dbName, e);
        }
        
        // Removed since it is not necessary.
        /*
        Kernel kernel = KernelRegistry.getSingleKernel();
        ManagementHelper helper = new KernelManagementHelper(kernel);
        ResourceAdapterModule[] modules = helper.getOutboundRAModules(helper.getDomains()[0].getServerInstances()[0], "javax.sql.DataSource");
        for (ResourceAdapterModule module : modules) {
            org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory[] databases = helper.getOutboundFactories(module, "javax.sql.DataSource");
            for (org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory db : databases) {
                try {
                    Object databaseName = db.getConfigProperty("DatabaseName");
                    if(dbName.equalsIgnoreCase((String) databaseName)) {
                        AbstractName tempDbName = helper.getNameFor(db);
                        return (DataSource) KernelRegistry.getSingleKernel().invoke(
                                tempDbName, "$getResource");
                    }
                } catch (Exception ignored) {
                }
            }
        }
        */
        return null;
    }

    /**
     * Get the datasource if dbName is == SYSTEM_DB, otherwise finds the datasource among JCAManagedConnectionFactories, otherwise returns null.
     *
     * @param dbName
     * @return datasource
     */
    public static DataSource getDataSource(String dsName) {
        
        Kernel kernel = KernelRegistry.getSingleKernel();
        
        try {
            if (SYSTEM_DATASOURCE_NAME!=null && ((String)SYSTEM_DATASOURCE_NAME.getName().get(NameFactory.J2EE_NAME)).equalsIgnoreCase(dsName)) {
            	return (DataSource) kernel.invoke(
            			SYSTEM_DATASOURCE_NAME, "$getResource");
            }
            
            AbstractNameQuery query = new AbstractNameQuery(GenericConnectionManager.class.getName());
            Set<AbstractName> names = KernelRegistry.getSingleKernel().listGBeans(query);
            for (AbstractName name : names) {
                String nameProperty = name.getNameProperty("name");
                if (dsName.equals(nameProperty)) {
                    return (DataSource) kernel.invoke(name, "$getResource");
                }
            }
            
        } catch (Exception e) {
        	log.error("Problem getting datasource " + dsName, e);
        }
        
        return null;
    }

}
