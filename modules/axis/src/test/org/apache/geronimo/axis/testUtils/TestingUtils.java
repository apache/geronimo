/*
 * Copyright 2003,2004 The Apache Software Foundation.
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

import java.net.URI;
import java.net.URL;
import java.util.Collections;

import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.axis.AxisGeronimoUtils;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.OnlineUserTransaction;

public class TestingUtils {
    
    protected static J2EEManager j2eeManager  = new J2EEManager();
    

    public static void startJ2EEContinerAndAxisServlet(Kernel kernel) throws Exception{
        //This does the work need to be done by plan
        j2eeManager.startJ2EEContainer(kernel);
        //start the Axis Serverlet which would be started by the service plan
        org.apache.geronimo.jetty.JettyWebAppContext c = null;
        GBeanMBean app = new GBeanMBean("org.apache.geronimo.jetty.JettyWebAppContext");
        URL url =
                Thread.currentThread().getContextClassLoader().getResource("deployables/axis/");
        System.out.print(url);
        app.setAttribute("uri", URI.create(url.toString()));
        app.setAttribute("contextPath", "/axis");
        app.setAttribute("componentContext", null);
        OnlineUserTransaction userTransaction = new OnlineUserTransaction();
        app.setAttribute("userTransaction", userTransaction);
        app.setAttribute("webClassPath", new URI[0]);
        app.setAttribute("contextPriorityClassLoader", Boolean.FALSE);
        app.setReferencePatterns("JettyContainer", Collections.singleton(AxisGeronimoConstants.WEB_CONTAINER_NAME));
        app.setAttribute("configurationBaseUrl", Thread.currentThread().getContextClassLoader().getResource("deployables/"));
        app.setReferencePattern("TransactionContextManager", AxisGeronimoConstants.TRANSACTION_CONTEXT_MANAGER_NAME);
        app.setReferencePattern("TrackedConnectionAssociator", AxisGeronimoConstants.TRACKED_CONNECTION_ASSOCIATOR_NAME);
        AxisGeronimoUtils.startGBean(AxisGeronimoConstants.APPLICATION_NAME, app, kernel);

    }
    
    public static void stopJ2EEContinerAndAxisServlet(Kernel kernel) throws Exception{
        j2eeManager.stopJ2EEContainer(kernel);
    }
    
    public static ResourceReferenceBuilder RESOURCE_REFERANCE_BUILDER = new ResourceReferenceBuilder() {

        public Reference createResourceRef(String containerId, Class iface) {
            return null;
        }

        public Reference createAdminObjectRef(String containerId, Class iface) {
            return null;
        }

        public ObjectName locateResourceName(ObjectName query) {
            return AxisGeronimoConstants.RESOURCE_ADAPTER_NAME;
        }

        public GBeanData locateActivationSpecInfo(ObjectName resourceAdapterModuleName, String messageListenerInterface) {
            return AxisGeronimoConstants.ACTIVATION_SPEC_INFO;
        }

        public GBeanData locateResourceAdapterGBeanData(ObjectName resourceAdapterModuleName) throws DeploymentException {
            return null;
        }

        public GBeanData locateAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName) throws DeploymentException {
            return null;
        }

        public GBeanData locateConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName) throws DeploymentException {
            return null;
        }
    };

}
