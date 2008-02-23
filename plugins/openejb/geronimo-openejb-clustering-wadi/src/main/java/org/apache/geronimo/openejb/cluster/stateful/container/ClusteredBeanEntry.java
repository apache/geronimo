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

package org.apache.geronimo.openejb.cluster.stateful.container;

import org.apache.geronimo.clustering.Session;
import org.apache.openejb.core.stateful.BeanEntry;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class ClusteredBeanEntry extends BeanEntry {
    protected static final String SESSION_KEY_ENTRY = "entry";

    private transient final Session session;
    private final Object deploymentId;

    protected static ClusteredBeanEntry getEntry(Session session) {
        if (null == session) {
            throw new IllegalArgumentException("session is required");
        }
        return (ClusteredBeanEntry) session.getState(SESSION_KEY_ENTRY);
    }

    protected ClusteredBeanEntry(Session session,
        Object deploymentId,
        Object beanInstance,
        Object primaryKey,
        long timeOut) {
        super(beanInstance, primaryKey, timeOut);
        if (null == session) {
            throw new IllegalArgumentException("session is required");
        } else if (null == deploymentId) {
            throw new IllegalArgumentException("deploymentId is required");
        }
        this.session = session;
        this.deploymentId = deploymentId;
        
        session.addState(SESSION_KEY_ENTRY, this);
    }

    protected ClusteredBeanEntry(Session session) {
        super(getEntry(session));
        this.session = session;
        this.deploymentId = getEntry(session).deploymentId;
    }

    public void release() {
        session.release();
    }

    public void endAccess() {
        session.onEndAccess();
    }
    
    public Object getDeploymentId() {
        return deploymentId;
    }

}
