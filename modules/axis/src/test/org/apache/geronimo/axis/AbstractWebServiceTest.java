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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;

import javax.management.ObjectName;
import javax.naming.Reference;

import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.axis.testUtils.TestingUtils;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.j2ee.deployment.ResourceReferenceBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.configuration.LocalConfigStore;
import org.openejb.deployment.OpenEJBModuleBuilder;

public class AbstractWebServiceTest extends AbstractTestCase {

    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");

    protected ObjectName axisname;
    protected Kernel kernel;
    protected LocalConfigStore store;
    protected File outFile = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);

    /**
     * @param testName
     */
    public AbstractWebServiceTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);

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
        TestingUtils.startJ2EEContinerAndAxisServlet(kernel);

    }

    protected void tearDown() throws Exception {
        TestingUtils.stopJ2EEContinerAndAxisServlet(kernel);
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }
    
     
    
    protected EARConfigBuilder getEARConfigBuilder() throws Exception {
        URI defaultParentId = new URI("org/apache/geronimo/Server");
        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(null, defaultParentId, null);
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
                        TestingUtils.RESOURCE_REFERANCE_BUILDER,
                        null,
                        null);
        return earConfigBuilder;
    }
}
