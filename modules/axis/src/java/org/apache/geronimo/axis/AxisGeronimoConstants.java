/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.axis;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * Class AxisGeronimoConstants
 */
public class AxisGeronimoConstants {

    /**
     * Field J2EE_DOMAIN_NAME
     */
    public static final String J2EE_DOMAIN_NAME = "openejb.server";

    /**
     * Field J2EE_SERVER_NAME
     */
    public static final String J2EE_SERVER_NAME = "TestOpenEJBServer";

    /**
     * Field WEB_CONTANER_NAME
     */
    public static final String WEB_CONTANER_NAME =
            "geronimo.jetty:role=Container";

    /**
     * Field WEB_CONNECTOR_NAME
     */
    public static final String WEB_CONNECTOR_NAME =
            "geronimo.jetty:role=Connector";

    /**
     * Field APPLICATION_NAME
     */
    public static final String APPLICATION_NAME = "geronimo.jetty:app=test";

    /**
     * Field TRANSACTION_MANAGER_NAME
     */
    public static final String TRANSACTION_MANAGER_NAME =
            "geronimo.test:role=TransactionManager";
    public static final String TRANSACTION_CONTEXT_MANAGER_NAME = "geronimo.test:role=TransactionContextManager";
    /**
     * Field CONNTECTION_TRACKING_COORDINATOR
     */
    public static final String CONNTECTION_TRACKING_COORDINATOR =
            "geronimo.test:role=ConnectionTrackingCoordinator";

    /**
     * Field AXIS_CONFIG_STORE
     */
    public static final String AXIS_CONFIG_STORE = "target/config-store";

    /**
     * Field TEMP_OUTPUT
     */
    public static final String TEMP_OUTPUT = "target/temp";

    /**
     * Field AXIS_SERVICE_PORT
     */
    public static final int AXIS_SERVICE_PORT = 5678;
    
    public static final String J2EE_SERVER_OBJECT_NAME  = J2EE_DOMAIN_NAME + ":j2eeType=J2EEServer,name=" + J2EE_SERVER_NAME;
    
    public static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    public static final ObjectName TRANSACTIONMANAGER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=TransactionManager");
    public static final ObjectName TRANSACTIONCONTEXTMANAGER_NAME = JMXUtil.getObjectName(J2EE_SERVER_NAME + ":type=TransactionContextManager");
    public static final ObjectName TRACKEDCONNECTIONASSOCIATOR_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    public static final ObjectName WORKMANAGER_NAME = JMXUtil.getObjectName("geronimo.server:type=WorkManager,name=DefaultWorkManager");
    public static final ObjectName RESOURCE_ADAPTER_NAME = JMXUtil.getObjectName("openejb.server:j2eeType=ResourceAdapter,J2EEServer=TestOpenEJBServer,name=MockRA");
    public static final ObjectName ACTIVATIONSPEC_NAME = JMXUtil.getObjectName("geronimo.server:j2eeType=ActivationSpec,name=MockMDB");
    public static final ObjectName THREADPOOL_NAME = JMXUtil.getObjectName(J2EE_SERVER_NAME + ":type=ThreadPool,name=DefaultThreadPool");
    public static final ObjectName TRANSACTIONALTIMER_NAME = JMXUtil.getObjectName(J2EE_SERVER_NAME + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    public static final ObjectName NONTRANSACTIONALTIMER_NAME = JMXUtil.getObjectName(J2EE_SERVER_NAME + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");

}
