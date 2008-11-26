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

import java.sql.Driver;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverPropertyInfo;
import java.sql.DriverManager;
import java.util.Properties;

import javax.sql.DataSource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceDriver implements Driver {

    private static final String BASE = "jdbc:geronimo:datasource:";

    static {
        try {
            DriverManager.registerDriver(new DataSourceDriver());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Connection connect(String url, Properties info) throws SQLException {
        if (url == null || !url.startsWith(BASE)) {
            return null;
        }
        String name = url.substring(BASE.length());
        DataSource ds;
        try {
            Context ctx = new InitialContext();
            try {
                ds = (DataSource) ctx.lookup("java:comp/env/" + name);
            } catch (NameNotFoundException e) {
                ds = (DataSource) ctx.lookup(name);
            }
        } catch (NamingException e) {
            throw (SQLException)new SQLException("Could not look up datasource").initCause(e);
        }
        return ds.getConnection();
    }

    public boolean acceptsURL(String url) throws SQLException {
        return url != null && url.startsWith(BASE);
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
        //lie shamelessly
        return true;
    }
}
