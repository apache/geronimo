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

package org.apache.geronimo.connector.outbound.connectiontracking;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.manager.MonitorableTransactionManager;

/**
 * 
 * @version $Revision$
 */
public class ConnectionTrackingCoordinatorGBean extends ConnectionTrackingCoordinator implements GBeanLifecycle {
    private final MonitorableTransactionManager monitorableTm;
    private final GeronimoTransactionListener listener;

    public ConnectionTrackingCoordinatorGBean(MonitorableTransactionManager monitorableTm, boolean lazyConnect) {
        super(lazyConnect);
        this.monitorableTm = monitorableTm;
        listener = new GeronimoTransactionListener(this);
    }

    public void doStart() throws Exception {
        monitorableTm.addTransactionAssociationListener(listener);
    }

    public void doStop() throws Exception {
        monitorableTm.removeTransactionAssociationListener(listener);
    }

    public void doFail() {
        monitorableTm.removeTransactionAssociationListener(listener);
    }

    public final static GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ConnectionTrackingCoordinatorGBean.class, NameFactory.JCA_CONNECTION_TRACKER);

        infoFactory.addReference("TransactionManager", MonitorableTransactionManager.class, NameFactory.JTA_RESOURCE);
        infoFactory.addAttribute("lazyConnect", boolean.class, true);

        infoFactory.addInterface(TrackedConnectionAssociator.class);
        infoFactory.addInterface(ConnectionTracker.class);

        infoFactory.setConstructor(new String[] {"TransactionManager", "lazyConnect"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
