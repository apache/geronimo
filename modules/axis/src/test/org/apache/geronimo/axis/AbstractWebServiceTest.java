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
    protected File outFile = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);

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
        wsConfgBuilderbean.setReferencePattern("AxisGbean",axisname);
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
//        GBeanMBean moduleBuilder = new GBeanMBean(OpenEJBModuleBuilder.GBEAN_INFO);
//        moduleBuilder.setAttribute("defaultParentId",defaultParentId);
//        kernel.loadGBean(AxisGeronimoConstants.OPENEJB_MODULE_BUILDER_NAME,moduleBuilder);
//        
//        
//        //<gbean name="geronimo.deployer:role=Builder,type=EAR,config=org/apache/geronimo/J2EEDeployer" class="org.apache.geronimo.j2ee.deployment.EARConfigBuilder">
//        //    <attribute name="defaultParentId">org/apache/geronimo/Server</attribute>
//        //    <attribute name="j2eeServer" type="javax.management.ObjectName">geronimo.server:j2eeType=J2EEServer,name=geronimo</attribute>
//        //    <attribute name="transactionContextManagerObjectName" type="javax.management.ObjectName">geronimo.server:type=TransactionContextManager</attribute>
//        //    <attribute name="connectionTrackerObjectName" type="javax.management.ObjectName">geronimo.server:type=ConnectionTracker</attribute>
//        //    <attribute name="transactionalTimerObjectName" type="javax.management.ObjectName">geronimo.server:type=ThreadPooledTimer,name=TransactionalThreadPooledTimer</attribute>
//        //    <attribute name="nonTransactionalTimerObjectName" type="javax.management.ObjectName">geronimo.server:type=ThreadPooledTimer,name=NonTransactionalThreadPooledTimer</attribute>
//        //    <reference name="Repository">*:role=Repository,*</reference>
//        //    <reference name="EJBConfigBuilder">geronimo.deployer:role=ModuleBuilder,type=EJB,config=org/apache/geronimo/J2EEDeployer</reference>
//        //    <reference name="EJBReferenceBuilder">geronimo.deployer:role=ModuleBuilder,type=EJB,config=org/apache/geronimo/J2EEDeployer</reference>
//        //    <reference name="WebConfigBuilder">geronimo.deployer:role=ModuleBuilder,type=Web,config=org/apache/geronimo/J2EEDeployer</reference>
//        //    <reference name="ConnectorConfigBuilder">geronimo.deployer:role=ModuleBuilder,type=Connector,config=org/apache/geronimo/J2EEDeployer</reference>
//        //    <reference name="ResourceReferenceBuilder">geronimo.deployer:role=ModuleBuilder,type=Connector,config=org/apache/geronimo/J2EEDeployer</reference>
//        //    <reference name="AppClientConfigBuilder">geronimo.deployer:role=ModuleBuilder,type=AppClient,config=org/apache/geronimo/J2EEDeployer</reference>
//        //</gbean>
//        
//        GBeanMBean earmoduleBuilder = new GBeanMBean(EARConfigBuilder.GBEAN_INFO);
//        moduleBuilder.setAttribute("defaultParentId",defaultParentId);
//        moduleBuilder.setAttribute("transactionContextManagerObjectName",AxisGeronimoConstants.TRANSACTION_CONTEXT_MANAGER_NAME);
//        moduleBuilder.setAttribute("connectionTrackerObjectName",AxisGeronimoConstants.CONNECTION_TRACKER_NAME);
//        moduleBuilder.setReferencePattern("EJBConfigBuilder",AxisGeronimoConstants.OPENEJB_MODULE_BUILDER_NAME);
//        moduleBuilder.setReferencePattern("EJBReferenceBuilder",AxisGeronimoConstants.OPENEJB_MODULE_BUILDER_NAME);
//        moduleBuilder.setReferencePattern("ResourceReferenceBuilder",AxisGeronimoConstants.OPENEJB_MODULE_BUILDER_NAME);
//        
//        
//        kernel.loadGBean(AxisGeronimoConstants.EAR_CONF_BUILDER_NAME,moduleBuilder);
//        
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
