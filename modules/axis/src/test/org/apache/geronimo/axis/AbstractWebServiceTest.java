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

import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.axis.testUtils.TestingUtils;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.openejb.deployment.OpenEJBModuleBuilder;

/**
 * 
 * @version $Rev: $ $Date: $
 */
public class AbstractWebServiceTest extends AbstractTestCase {

    private static final String j2eeDomainName = "openejb.server";
    private static final String j2eeServerName = "TestOpenEJBServer";
    private static final ObjectName transactionManagerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");
    private static final ObjectName connectionTrackerObjectName = JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");

    protected ObjectName axisname;
    protected ObjectName wsConfgBuilderName;
    protected Kernel kernel;
    protected ConfigurationStore store;
    protected File outFile = new File("target/temp");

    /**
     * @param testName
     */
    public AbstractWebServiceTest(String testName) throws FileNotFoundException, WaitingException, IOException {
        super(testName);

        store = AxisGeronimoConstants.STORE;

    }

    protected void setUp() throws Exception {
        axisname = new ObjectName("test:name=AxisGBean");
        wsConfgBuilderName = new ObjectName("test:name=wsConfgBuilder");
        kernel = new Kernel("test.kernel");
        kernel.boot();
        //start the J2EE server which would be started by the server plan
        //in the real case 
        TestingUtils.startJ2EEContinerAndAxisServlet(kernel);
        
        //Start axis gbean        
        GBeanMBean axisgbean = new GBeanMBean(AxisGbean.getGBeanInfo());
        kernel.loadGBean(axisname, axisgbean);
        kernel.startGBean(axisname);
        
        GBeanMBean wsConfgBuilderbean = new GBeanMBean(WSConfigBuilder.getGBeanInfo());
        //wsConfgBuilderbean.setReferencePattern("AxisGBean",axisname);
        kernel.loadGBean(wsConfgBuilderName, wsConfgBuilderbean);
        kernel.startGBean(wsConfgBuilderName);
        
        

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
