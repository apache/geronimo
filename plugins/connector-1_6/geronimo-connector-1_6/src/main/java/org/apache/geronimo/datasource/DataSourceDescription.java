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

package org.apache.geronimo.datasource;

import java.io.Serializable;
import java.util.Map;

/**
 * @version $Revision$
 */
public class DataSourceDescription implements Serializable {
    
    // standard settings
    private String name;
    private String className;
    private String description;
    private String url;
    private String user;
    private String password;
    private String databaseName;
    private String serverName;
    private int portNumber = -1;
    private int loginTimeout = 0;
    private Map<String, String> properties;
    
    // transaction settings
    private boolean transactional = true;
    private int isolationLevel = -1;
  
    // pool settings
    private int initialPoolSize = -1;
    private int maxPoolSize = -1;
    private int minPoolSize = -1;
    private int maxIdleTime = -1;
    private int maxStatements = -1;

    //extra geronimo properties
    private int blockingTimeoutMilliseconds = -1;
    private boolean xaTransactionCaching = true;
    private boolean xaThreadCaching = false;
    private String osgiServiceName;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDatabaseName() {
        return databaseName;
    }
    
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }
    
    public String getServerName() {
        return serverName;
    }
    
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }
    
    public int getPortNumber() {
        return portNumber;
    }
    
    public void setPortNumber(int portNumber) {
        this.portNumber = portNumber;
    }
    
    public int getLoginTimeout() {
        return loginTimeout;
    }
    
    public void setLoginTimeout(int loginTimeout) {
        this.loginTimeout = loginTimeout;
    }
    
    public boolean isTransactional() {
        return transactional;
    }
    
    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public int getIsolationLevel() {
        return isolationLevel;
    }

    public void setIsolationLevel(int isolationLevel) {
        this.isolationLevel = isolationLevel;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public int getInitialPoolSize() {
        return initialPoolSize;
    }

    public void setInitialPoolSize(int initialPoolSize) {
        this.initialPoolSize = initialPoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getMinPoolSize() {
        return minPoolSize;
    }

    public void setMinPoolSize(int minPoolSize) {
        this.minPoolSize = minPoolSize;
    }

    public int getMaxIdleTime() {
        return maxIdleTime;
    }

    public void setMaxIdleTime(int maxIdleTime) {
        this.maxIdleTime = maxIdleTime;
    }

    public int getMaxStatements() {
        return maxStatements;
    }

    public void setMaxStatements(int maxStatements) {
        this.maxStatements = maxStatements;
    }

    public int getBlockingTimeoutMilliseconds() {
        return blockingTimeoutMilliseconds;
    }

    public void setBlockingTimeoutMilliseconds(int blockingTimeoutMilliseconds) {
        this.blockingTimeoutMilliseconds = blockingTimeoutMilliseconds;
    }

    public String getOsgiServiceName() {
        return osgiServiceName;
    }

    public void setOsgiServiceName(String osgiServiceName) {
        this.osgiServiceName = osgiServiceName;
    }

    public boolean isXaThreadCaching() {
        return xaThreadCaching;
    }

    public void setXaThreadCaching(boolean xaThreadCaching) {
        this.xaThreadCaching = xaThreadCaching;
    }

    public boolean isXaTransactionCaching() {
        return xaTransactionCaching;
    }

    public void setXaTransactionCaching(boolean xaTransactionCaching) {
        this.xaTransactionCaching = xaTransactionCaching;
    }

    public boolean hasStandardProperties() {
        return (databaseName != null
                || password != null
                || user != null
                || portNumber != -1
                || !(serverName != null && serverName.equals("localhost")));
    }
    
    public boolean hasPoolingProperties() {
        return (maxPoolSize != -1
                || minPoolSize != -1
                || maxIdleTime != -1
                || initialPoolSize != -1
                || maxStatements != -1);
    }
          
}
