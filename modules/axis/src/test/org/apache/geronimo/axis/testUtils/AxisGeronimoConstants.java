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
package org.apache.geronimo.axis.testUtils;

import java.io.File;

import javax.management.ObjectName;

import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.configuration.LocalConfigStore;

/**
 * @version $Rev: $ $Date: $
 * @version $Rev: $ $Date: $
 */

public class AxisGeronimoConstants {
    public static final String J2EE_DOMAIN_NAME = "geronimo.server";
    public static final String J2EE_SERVER_STRING = "geronimo";
    
    public static final ObjectName APPLICATION_NAME
            = JMXUtil.getObjectName("geronimo.jetty:app=test");
    public static final String AXIS_CONFIG_STORE = "target/config-store";
    public static final String TEMP_OUTPUT = "target/temp";

    public static final ObjectName J2EE_SERVER_INFO = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=ServerInfo");
    public static final ObjectName J2EE_SERVER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":j2eeType=J2EEServer,name=" + J2EE_SERVER_STRING);
    
    
    public static final ObjectName CONNECTION_TRACKER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=ConnectionTracker");
    
    public static final ObjectName WEB_CONTAINER_NAME = JMXUtil.getObjectName("geronimo.jetty:role=Container");
    public static final ObjectName WEB_CONNECTOR_NAME = JMXUtil.getObjectName("geronimo.jetty:role=Connector");

    public static final ObjectName EJB_CONTAINER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=ContainerIndex");

    public static final ObjectName TRANSACTION_MANAGER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=TransactionManager");
    public static final ObjectName TRANSACTION_CONTEXT_MANAGER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=TransactionContextManager");
    public static final ObjectName TRACKED_CONNECTION_ASSOCIATOR_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":role=TrackedConnectionAssociator");
    public static final ObjectName WORKMANAGER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME+ ":type=WorkManager,name=DefaultWorkManager");
    
    
    
    public static final ObjectName RESOURCE_ADAPTER_NAME = JMXUtil.getObjectName("openejb.server:j2eeType=ResourceAdapter,J2EEServer=TestOpenEJBServer,name=MockRA");
    public static final ObjectName ACTIVATIONSPEC_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":j2eeType=ActivationSpec,name=MockMDB");
    public static final ObjectName THREADPOOL_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=ThreadPool,name=DefaultThreadPool");
    public static final ObjectName TRANSACTIONAL_TIMER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=ThreadPooledTimer,name=TransactionalThreaPooledTimer");
    public static final ObjectName NONTRANSACTIONAL_TIMER_NAME = JMXUtil.getObjectName(J2EE_DOMAIN_NAME + ":type=ThreadPooledTimer,name=NonTransactionalThreaPooledTimer");
    public static final GBeanData ACTIVATION_SPEC_INFO = new GBeanData(ActivationSpecWrapper.getGBeanInfo());
    
    public static File OUTFILE = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
    public static LocalConfigStore STORE = null;
    
    public static ObjectName OPENEJB_MODULE_BUILDER_NAME = JMXUtil.getObjectName("geronimo.deployer:role=ModuleBuilder,type=EJB,config=org/apache/geronimo/Server");
    public static ObjectName EAR_CONF_BUILDER_NAME = JMXUtil.getObjectName("geronimo.deployer:role=Builder,type=EAR,config=org/apache/geronimo/J2EEDeployer");
    
    static{
        try{
            STORE = new LocalConfigStore(OUTFILE);
            STORE.doStart();    
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    
}
