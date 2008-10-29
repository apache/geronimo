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

import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class StatefulContainerGBean extends EjbContainer {

    /**
     * Maximum number of values that should be in the LRU
     */
    private final int capacity;

    /**
     * When the LRU is exceeded, this is the is the number of beans stored.
     * This helps to avoid passivating a bean at a time.
     */
    private final int bulkPassivate;

    /**
     * A bean may be destroyed if it isn't used in this length of time (in
     * milliseconds).
     */
    private final long timeout;


    public StatefulContainerGBean(
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName,
            @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
            @ParamAttribute(name = "provider") String provider,
            @ParamAttribute(name = "bulkPassivate") int bulkPassivate,
            @ParamAttribute(name = "capacity") int capacity,
            @ParamAttribute(name = "timeout") long timeout,
            @ParamAttribute(name = "properties") Properties properties) {
        super(abstractName, StatefulSessionContainerInfo.class, openEjbSystem, provider, "STATEFUL", properties);
        set("BulkPassivate", Integer.toString(bulkPassivate));
        set("Capacity", Integer.toString(capacity));
        set("TimeOut", Long.toString(timeout));
        this.bulkPassivate = bulkPassivate;
        this.capacity = capacity;
        this.timeout = timeout;
    }

    public int getBulkPassivate() {
        return bulkPassivate;
    }

    public int getCapacity() {
        return capacity;
    }

    public long getTimeout() {
        return timeout;
    }
}
