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


package org.apache.geronimo.cli.deployer;

/**
 * @version $Rev$ $Date$
 */
public class ConnectionParamsImpl implements ConnectionParams {

    private String uri;
    private String host;
    private Integer port;
    private String driver;
    private String user;
    private String password;
    private boolean syserr;
    private boolean verbose;
    private boolean offline;
    private boolean secure;


    public ConnectionParamsImpl(String uri, String host, Integer port, String driver, String user, String password, boolean syserr, boolean verbose, boolean offline) {
        this(uri, host, port, driver, user, password, syserr, verbose, offline, false);
    }
    
    public ConnectionParamsImpl(String uri, String host, Integer port, String driver, String user, String password, boolean syserr, boolean verbose, boolean offline, boolean secure) {
        this.uri = uri;
        this.host = host;
        this.port = port;
        this.driver = driver;
        this.user = user;
        this.password = password;
        this.syserr = syserr;
        this.verbose = verbose;
        this.offline = offline;
        this.secure = secure;
    }

    public ConnectionParamsImpl() {
    }

    public String getURI() {
        return uri;
    }

    public void setURI(String uri) {
        this.uri = uri;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
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

    public boolean isSyserr() {
        return syserr;
    }

    public void setSyserr(boolean syserr) {
        this.syserr = syserr;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isOffline() {
        return offline;
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
    
    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
