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
package org.apache.geronimo.connector.outbound;

import javax.resource.spi.ConnectionManager;
import javax.transaction.TransactionManager;

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.proxy.ProxyManager;

/**
 * @version $Revision$
 */
public class GenericConnectionManagerGBean extends GenericConnectionManager implements GBeanLifecycle {
    private final Kernel kernel;

    public GenericConnectionManagerGBean() {
        super();
        kernel = null;
    }

    public GenericConnectionManagerGBean(TransactionSupport transactionSupport,
                                         PoolingSupport pooling,
                                         boolean containerManagedSecurity,
                                         ConnectionTracker connectionTracker,
                                         TransactionManager transactionManager,
                                         String objectName,
                                         ClassLoader classLoader,
                                         Kernel kernel) {
        super(transactionSupport, pooling, containerManagedSecurity, connectionTracker, transactionManager, objectName, classLoader);
        this.kernel = kernel;
    }

    public ConnectionManager getConnectionManager() {
        ConnectionManager unproxied = super.getConnectionManager();
        ProxyManager pm = kernel.getProxyManager();
        if(pm.isProxy(unproxied)) {
            return unproxied;
        } else {
            return (ConnectionManager) pm.createProxy(kernel.getAbstractNameFor(unproxied), unproxied.getClass().getClassLoader());
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(GenericConnectionManagerGBean.class, AbstractConnectionManagerGBean.GBEAN_INFO);

        infoBuilder.addAttribute("transactionSupport", TransactionSupport.class, true);
        infoBuilder.addAttribute("pooling", PoolingSupport.class, true);
        infoBuilder.addAttribute("containerManagedSecurity", Boolean.TYPE, true);

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addAttribute("kernel", Kernel.class, false);

        infoBuilder.addReference("ConnectionTracker", ConnectionTracker.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoBuilder.addReference("TransactionManager", TransactionManager.class, NameFactory.TRANSACTION_MANAGER);


        infoBuilder.setConstructor(new String[]{
            "transactionSupport",
            "pooling",
            "containerManagedSecurity",
            "ConnectionTracker",
            "TransactionManager",
            "objectName",
            "classLoader",
            "kernel"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
