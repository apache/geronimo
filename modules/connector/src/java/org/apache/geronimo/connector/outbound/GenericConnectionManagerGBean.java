/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.connector.outbound;

import org.apache.geronimo.connector.outbound.connectionmanagerconfig.PoolingSupport;
import org.apache.geronimo.connector.outbound.connectionmanagerconfig.TransactionSupport;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTracker;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.transaction.context.TransactionContextManager;

/**
 * @version $Revision$
 */
public class GenericConnectionManagerGBean extends GenericConnectionManager implements GBeanLifecycle {


    public GenericConnectionManagerGBean() {
        super();
    }

    public GenericConnectionManagerGBean(TransactionSupport transactionSupport,
                                         PoolingSupport pooling,
                                         boolean containerManagedSecurity,
                                         ConnectionTracker connectionTracker,
                                         TransactionContextManager transactionContextManager,
                                         String objectName,
                                         ClassLoader classLoader) {
        super(transactionSupport, pooling, containerManagedSecurity, connectionTracker, transactionContextManager, objectName, classLoader);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(GenericConnectionManagerGBean.class, AbstractConnectionManagerGBean.GBEAN_INFO);

        infoBuilder.addAttribute("transactionSupport", TransactionSupport.class, true);
        infoBuilder.addAttribute("pooling", PoolingSupport.class, true);
        infoBuilder.addAttribute("containerManagedSecurity", Boolean.TYPE, true);

        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);

        infoBuilder.addReference("ConnectionTracker", ConnectionTracker.class, NameFactory.JCA_CONNECTION_TRACKER);
        infoBuilder.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);


        infoBuilder.setConstructor(new String[]{
            "transactionSupport",
            "pooling",
            "containerManagedSecurity",
            "ConnectionTracker",
            "TransactionContextManager",
            "objectName",
            "classLoader"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
