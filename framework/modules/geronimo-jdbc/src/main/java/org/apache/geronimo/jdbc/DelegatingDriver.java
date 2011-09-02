/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jdbc;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.sql.Driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverPropertyInfo;
import java.sql.DriverManager;

/**
 * Class to sneak around idiotic classloading restrictions in DriverManager.  This basically does the same as DriverManager
 * except that you register Driver instances directly.
 * @version $Rev$ $Date$
 */
public class DelegatingDriver implements Driver {

    private static final List<Driver> DRIVERS = new CopyOnWriteArrayList<Driver>();
    
    static final DelegatingDriver DELEGATINGDRIVER_INSTANCE; 

    static {
        try {
            DELEGATINGDRIVER_INSTANCE = new DelegatingDriver();
            DriverManager.registerDriver(DELEGATINGDRIVER_INSTANCE);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerDriver(Driver instance) {
        DRIVERS.add(instance);
    }

    public static void unregisterDriver(Driver instance) {
        DRIVERS.remove(instance);
    }

    public Connection connect(String url, Properties info) throws SQLException {
        for (Driver driver: DRIVERS) {
            Connection conn = driver.connect(url, info);
            if (conn != null) {
                return conn;
            }
        }
        return null;
    }

    public boolean acceptsURL(String url) throws SQLException {
        for (Driver driver: DRIVERS) {
            if (driver.acceptsURL(url)) {
                return true;
            }
        }
        return false;
    }

    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    public int getMajorVersion() {
        return 1;
    }

    public int getMinorVersion() {
        return 0;
    }

    public boolean jdbcCompliant() {
        //Lie through our teeth
        return true;
    }
}
