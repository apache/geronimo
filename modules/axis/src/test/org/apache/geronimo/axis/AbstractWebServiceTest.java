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

import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.axis.testUtils.J2EEManager;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.apache.geronimo.transaction.OnlineUserTransaction;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.openejb.deployment.OpenEJBModuleBuilder;

import javax.management.ObjectName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;

public class AbstractWebServiceTest extends AbstractTestCase {

    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");

    protected ObjectName axisname;
    protected Kernel kernel;
    protected J2EEManager j2eeManager;
    protected LocalConfigStore store;
    protected File outFile = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);

    /**
     * @param testName
     */
    public AbstractWebServiceTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);
        j2eeManager = new J2EEManager();
        store = new LocalConfigStore(outFile);
        store.doStart();
    }

    protected void setUp() throws Exception {
        axisname = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        AxisGeronimoUtils.delete(outFile);
        outFile.getParentFile().mkdirs();
        //start the J2EE server which would be started by the server plan
        //in the real case 
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

    protected void tearDown() throws Exception {
        j2eeManager.stopJ2EEContainer(kernel);
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }

    protected EARConfigBuilder getEARConfigBuilder() throws Exception {
        URI defaultParentId = new URI("org/apache/geronimo/Server");
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(defaultParentId, null);
        EARConfigBuilder earConfigBuilder =
                new EARConfigBuilder(defaultParentId,
                        new ObjectName(j2eeDomainName + ":j2eeType=J2EEServer,name=" + j2eeServerName),
                        transactionManagerObjectName,
                        connectionTrackerObjectName,
                        null,
                        null,
                        null,
                        moduleBuilder,
                        moduleBuilder,
                        null,
                        null,
                        null,
                        null,
                        null);
        return earConfigBuilder;
    }
}
