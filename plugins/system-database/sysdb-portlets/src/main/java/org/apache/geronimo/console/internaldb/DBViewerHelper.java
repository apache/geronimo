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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import org.apache.geronimo.console.util.KernelManagementHelper;
import org.apache.geronimo.console.util.ManagementHelper;
import org.apache.geronimo.console.util.PortletManager;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.apache.geronimo.management.geronimo.ResourceAdapterModule;

public class DBViewerHelper {

    private static final String SYS_TBL_PREFIX = "SYS.";

    private static final int COUNT_COL = 1;

    /**
     * List the databases having datasources deployed.
     *
     * @param derbySysHome
     * @return
     */
    public Collection<String> getDataSourceNames() {
    	
    	List<String> databaseNames = new ArrayList<String>();

        Kernel kernel = KernelRegistry.getSingleKernel();
        ManagementHelper helper = new KernelManagementHelper(kernel);
        ResourceAdapterModule[] modules = helper.getOutboundRAModules(helper.getDomains()[0].getServerInstances()[0], "javax.sql.DataSource");
        for (ResourceAdapterModule module : modules) {
            org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory[] databases = helper.getOutboundFactories(module, "javax.sql.DataSource");
            for (org.apache.geronimo.management.geronimo.JCAManagedConnectionFactory db : databases) {
                try {
                    AbstractName dbName = kernel.getAbstractNameFor(db);
                    String datasourceName = (String)dbName.getName().get(NameFactory.J2EE_NAME);
                    databaseNames.add(datasourceName);
                } catch (Exception ignored) {
                }
            }
        }

        return databaseNames;
    }
    
    /**
     * List the databases given the derby home directory.
     *
     * @param derbySysHome
     * @return
     */
    public Collection<String> getDerbyDatabases(String derbySysHome) {
        List<String> databases = new ArrayList<String>();
        File f = new File(derbySysHome);
        // Check if this is a directory
        if (f.isDirectory()) {
            // Check for folders only
            File[] files = f.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    databases.add(files[i].getName());
                }
            }
        }

        return databases;
    }

    /**
     * @param derbySysHome
     * @param dbName
     * @return
     */
    public boolean isDBValid(String derbySysHome, String dbName) {
        if ((derbySysHome == null) || (derbySysHome.trim().length() == 0)) {
            return false;
        }
        if ((dbName == null) || (dbName.trim().length() == 0)) {
            return false;
        }

        Collection databases = getDerbyDatabases(derbySysHome);
        return databases.contains(dbName);
    }

    /**
     * @param dbName
     * @param tblName
     * @return
     */
    public boolean isTblValid(String dbName, String tblName) {
        if ((dbName == null) || (dbName.trim().length() == 0)) {
            return false;
        }
        if ((tblName == null) || (tblName.trim().length() == 0)) {
            return false;
        }
        return true;

        // Removed this code because it doesn't seem necessary and causes a
        // weird ClassCastException when rs.next() is called.
        /*
         else {
         if (tblName.toUpperCase().startsWith(SYS_TBL_PREFIX)) {
         tblName = tblName.substring(SYS_TBL_PREFIX.length());
         }
         }

         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         try {
         conn = DerbyConnectionUtil.getDerbyConnection(dbName);
         ps = conn.prepareStatement("SELECT count(*) FROM SYS.SYSTABLES"
         + " WHERE tablename=?");
         ps.setString(1, tblName.toUpperCase());
         rs = ps.executeQuery();
         if (rs.next()) {
         int count = rs.getInt(COUNT_COL);
         if (count == 1) {
         return true;
         }
         }
         } catch (Throwable e) {
         e.printStackTrace();
         System.out.println("ERROR: " + e.getMessage());
         // Assume table is not valid
         return false;
         } finally {
         // close DB connection
         try {
         if (rs != null) {
         rs.close();
         }
         if (ps != null) {
         ps.close();
         }
         if (conn != null) {
         conn.close();
         }
         } catch (SQLException e) {
         // problem closing DB connection
         }
         }

         return false;
         */
    }

}
