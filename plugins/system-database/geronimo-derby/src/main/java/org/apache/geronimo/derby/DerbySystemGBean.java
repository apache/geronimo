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

package org.apache.geronimo.derby;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * A GBean that represents an instance of an Apache Derby system (a system being
 * a collection of different databases).
 *
 * @version $Rev$ $Date$
 */
public class DerbySystemGBean implements DerbySystem, GBeanLifecycle {
    private static final Logger log = LoggerFactory.getLogger(DerbySystemGBean.class);
    private static final String SYSTEM_HOME = "derby.system.home";
    private static final String SHUTDOWN_ALL = "jdbc:derby:;shutdown=true";

    private final ServerInfo serverInfo;
    private final String systemHome;
    private String actualHome;

    public DerbySystemGBean(ServerInfo serverInfo, String derbySystemHome) {
        this.serverInfo = serverInfo;
        this.systemHome = derbySystemHome;
    }

    public String getDerbyHome() {
        return actualHome;
    }

    public void doStart() throws Exception {
        // set up the system property for the database home
        actualHome = System.getProperty(SYSTEM_HOME);
        if (actualHome == null) {
            actualHome = serverInfo.resolveServerPath(systemHome);
        }
        System.setProperty(SYSTEM_HOME, actualHome);

        // set the magic system property that causes derby to use explicity
        // file sync instead of relying on vm support for file open rws
        System.setProperty("derby.storage.fileSyncTransactionLog", "true");

        // load the Embedded driver to initialize the home
        new org.apache.derby.jdbc.EmbeddedDriver();
        log.debug("Started in " + actualHome);
    }

    public void doStop() throws Exception {
        try {
            DriverManager.getConnection(SHUTDOWN_ALL, null, null);
        } catch (SQLException e) {
            // SQLException gets thrown on successful shutdown so ignore
        }
        System.gc();  // Added per recommendation Derby documentation
        log.debug("Stopped");
    }

    public void doFail() {
        try {
            DriverManager.getConnection(SHUTDOWN_ALL, null, null);
        } catch (SQLException e) {
            // SQLException gets thrown on successful shutdown so ignore
        }
        System.gc();  // Added per recommendation Derby documentation
        log.warn("Failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DerbySystemGBean.class);
        infoFactory.addAttribute("derbySystemHome", String.class, true);
        infoFactory.addAttribute("derbyHome", String.class, false);
        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");
        infoFactory.setConstructor(new String[]{"ServerInfo", "derbySystemHome"});
        infoFactory.setPriority(GBeanInfo.PRIORITY_CLASSLOADER);
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
}
