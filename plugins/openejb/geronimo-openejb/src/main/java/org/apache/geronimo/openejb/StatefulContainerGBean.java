/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.openejb.assembler.classic.StatefulSessionContainerInfo;
import org.apache.openejb.util.Duration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class StatefulContainerGBean extends EjbContainer {

    /**
     * Specifies the maximum time an invocation could wait for the stateful bean instance to become available before
     * giving up.
     * 
     * After the timeout is reached a javax.ejb.ConcurrentAccessTimeoutException will be thrown.
     */
    private long accessTimeout;   
    
    
    /**
     * Maximum number of values that should be in the LRU
     */
    private int capacity;

    /**
     * When the LRU is exceeded, this is the is the number of beans stored.
     * This helps to avoid passivating a bean at a time.
     */
    private int bulkPassivate;

    /**
     * A bean may be destroyed if it isn't used in this length of time (in
     * milliseconds).
     */
    private long timeOut;


    public StatefulContainerGBean(
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName,
            @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
            @ParamAttribute(name = "provider") String provider,
            @ParamAttribute(name = "bulkPassivate") int bulkPassivate,
            @ParamAttribute(name = "accessTimeout") long accessTimeout,
            @ParamAttribute(name = "capacity") int capacity,
            @ParamAttribute(name = "timeOut") long timeOut,
            @ParamAttribute(name = "properties") Properties properties) {
        super(abstractName, StatefulSessionContainerInfo.class, openEjbSystem, provider, "STATEFUL", properties);
        setAccessTimeout(accessTimeout);
        setCapacity(capacity);
        setBulkPassivate(bulkPassivate);
        setTimeOut(timeOut);
    }


    public long getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(long accessTimeout) {
        this.accessTimeout = accessTimeout;
        Duration accessTimeoutDuration = new Duration(accessTimeout, TimeUnit.SECONDS);
        set("AccessTimeout", accessTimeoutDuration.toString());
    }

    public int getBulkPassivate() {
        return bulkPassivate;
    }

    public void setBulkPassivate(int bulkPassivate) {
        this.bulkPassivate = bulkPassivate;
        set("BulkPassivate", Integer.toString(bulkPassivate));
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
        set("Capacity", Integer.toString(capacity));
    }
    
    public long getTimeOut() {
        return timeOut;
    }
    
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
        set("TimeOut", Long.toString(timeOut));
    }

}
