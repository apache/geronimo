/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.apache.taglibs.standard.tag.common.sql;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.taglibs.standard.resources.Resources;

/**
 * <p>A simple <code>DataSource</code> wrapper for the standard
 * <code>DriverManager</code> class.
 * 
 * @author Hans Bergsten
 */
public class DataSourceWrapper implements DataSource {
    private Driver driver;
    private String jdbcURL;
    private String userName;
    private String password;

    public void setDriverClassName(String driverClassName) 
        throws ClassNotFoundException, InstantiationException, 
            IllegalAccessException {

        //get the classloader
        ClassLoader cl;
        SecurityManager sm = System.getSecurityManager();
        if (sm == null) {
            cl = Thread.currentThread().getContextClassLoader();
        } else {
            cl = java.security.AccessController.doPrivileged(
                    new java.security.PrivilegedAction<ClassLoader>() 
                    {public ClassLoader run() {return Thread.currentThread().getContextClassLoader();}});
        }
        //done getting classloader
    
        Object instance = Class.forName(driverClassName, true, cl).newInstance();
        if (instance instanceof Driver) {
            driver = (Driver) instance;
        }
    }

    public void setJdbcURL(String jdbcURL) {
	this.jdbcURL = jdbcURL;
    }

    public void setUserName(String userName) {
	this.userName = userName;
    }

    public void setPassword(String password) {
	this.password = password;
    }

    /**
     * Returns a Connection using the DriverManager and all
     * set properties.
     */
    public Connection getConnection() throws SQLException {
        Connection conn = null;
        if (driver != null) {
            Properties props = new Properties();
            if (userName != null) {
                props.put("user", userName);
            }
            if (password != null) {
                props.put("password", password);
            }
            conn = driver.connect(jdbcURL, props);
        }
        if (conn == null) {
            if (userName != null) {
                conn = DriverManager.getConnection(jdbcURL, userName, password);
            } else {
                conn = DriverManager.getConnection(jdbcURL);
            }
        }
        return conn;
    }
    
    /**
     * Always throws a SQLException. Username and password are set
     * in the constructor and can not be changed.
     */
    public Connection getConnection(String username, String password) 
            throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }
    
    /**
     * Always throws a SQLException. Not supported.
     */
    public int getLoginTimeout() throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }
    
    /**
     * Always throws a SQLException. Not supported.
     */
    public PrintWriter getLogWriter() throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }
    
    /**
     * Always throws a SQLException. Not supported.
     */
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }
    
    /**
     * Always throws a SQLException. Not supported.
     */
    public synchronized void setLogWriter(PrintWriter out) throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }
    
    public synchronized boolean isWrapperFor(Class c) throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }

    public synchronized Object unwrap(Class c) throws SQLException {
        throw new SQLException(Resources.getMessage("NOT_SUPPORTED"));
    }

}
