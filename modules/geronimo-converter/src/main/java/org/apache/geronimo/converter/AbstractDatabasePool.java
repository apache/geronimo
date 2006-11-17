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
package org.apache.geronimo.converter;

import java.io.Serializable;

/**
 * A common intermediate format for a database connection pool
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDatabasePool implements Serializable {
    public final static String VENDOR_ORACLE = "Oracle";
    public final static String VENDOR_MYSQL = "MySQL";
    public final static String VENDOR_SYBASE = "Sybase";
    public final static String VENDOR_INFORMIX = "Informix";
    private String name;
    private String jndiName;
    private Integer minSize;
    private Integer maxSize;
    private Integer blockingTimeoutMillis;
    private Integer idleTimeoutMillis;
    private String newConnectionSQL;
    private String testConnectionSQL;
    private String vendor;
    private Integer statementCacheSize;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJndiName() {
        return jndiName;
    }

    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    public String getNewConnectionSQL() {
        return newConnectionSQL;
    }

    public void setNewConnectionSQL(String newConnectionSQL) {
        this.newConnectionSQL = newConnectionSQL;
    }

    public String getTestConnectionSQL() {
        return testConnectionSQL;
    }

    public void setTestConnectionSQL(String testConnectionSQL) {
        this.testConnectionSQL = testConnectionSQL;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getMinSize() {
        return minSize;
    }

    public void setMinSize(Integer minSize) {
        this.minSize = minSize;
    }

    public Integer getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(Integer maxSize) {
        this.maxSize = maxSize;
    }

    public Integer getBlockingTimeoutMillis() {
        return blockingTimeoutMillis;
    }

    public void setBlockingTimeoutMillis(Integer blockingTimeoutMillis) {
        this.blockingTimeoutMillis = blockingTimeoutMillis;
    }

    public Integer getIdleTimeoutMillis() {
        return idleTimeoutMillis;
    }

    public void setIdleTimeoutMillis(Integer idleTimeoutMillis) {
        this.idleTimeoutMillis = idleTimeoutMillis;
    }

    public Integer getStatementCacheSize() {
        return statementCacheSize;
    }

    public void setStatementCacheSize(Integer statementCacheSize) {
        this.statementCacheSize = statementCacheSize;
    }
}
