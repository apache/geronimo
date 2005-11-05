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

package org.apache.geronimo.console.databasemanager.mssql;

public class MSSQLInfo {
    // Used
    private String user;

    private String password;

    private String serverName;

    private String portNumber;

    private String databaseName;

    // Not used
    private String objectName;

    private String name;

    private String driver;

    private String connectionURL;

    private String exceptionSorterClass;

    /**
     * @return Returns the connectionURL.
     */
    public String getConnectionURL() {
        return connectionURL;
    }

    /**
     * @param connectionURL
     *            The connectionURL to set.
     */
    public void setConnectionURL(String connectionURL) {
        this.connectionURL = connectionURL;
    }

    /**
     * @return Returns the exceptionSorterClass.
     */
    public String getExceptionSorterClass() {
        return exceptionSorterClass;
    }

    /**
     * @param exceptionSorterClass
     *            The exceptionSorterClass to set.
     */
    public void setExceptionSorterClass(String exceptionSorterClass) {
        this.exceptionSorterClass = exceptionSorterClass;
    }

    /**
     * @return Returns the databaseName.
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * @param databaseName
     *            The databaseName to set.
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the objectName.
     */
    public String getObjectName() {
        return objectName;
    }

    /**
     * @param objectName
     *            The objectName to set.
     */
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return Returns the portNumber.
     */
    public String getPortNumber() {
        return portNumber;
    }

    /**
     * @param portNumber
     *            The portNumber to set.
     */
    public void setPortNumber(String portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * @return Returns the serverName.
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @param serverName
     *            The serverName to set.
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user
     *            The user to set.
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return Returns the driver.
     */
    public String getDriver() {
        return driver;
    }

    /**
     * @param driver
     *            The driver to set.
     */
    public void setDriver(String driver) {
        this.driver = driver;
    }
}
