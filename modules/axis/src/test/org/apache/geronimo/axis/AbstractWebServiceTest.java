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
import java.net.URI;

import javax.management.ObjectName;

import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.axis.testUtils.TestingUtils;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARConfigBuilder;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationStore;
import org.apache.geronimo.kernel.jmx.JMXUtil;
//import org.openejb.deployment.OpenEJBModuleBuilder;

/**
 * 
 * @version $Rev: $ $Date: $
 */
public class AbstractWebServiceTest extends AbstractTestCase {

    protected ObjectName axisname;
    protected ObjectName wsConfgBuilderName;
    protected Kernel kernel;
    protected ConfigurationStore store;
    protected File outFile = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);

    /**
     * @param testName
     */
    public AbstractWebServiceTest(String testName) {
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
//        TestingUtils.startJ2EEContainerAndAxisServlet(kernel);
        
        //Start axis gbean        
        GBeanData axisgbData = new GBeanData(axisname,AxisGbean.getGBeanInfo());
        kernel.loadGBean(axisgbData, Thread.currentThread().getContextClassLoader());
        kernel.startGBean(axisname);
        
        GBeanData wsConfgBuilderbean = new GBeanData(wsConfgBuilderName,WSConfigBuilder.getGBeanInfo());
        wsConfgBuilderbean.setReferencePattern("AxisGbean",axisname);
        kernel.loadGBean(wsConfgBuilderbean,Thread.currentThread().getContextClassLoader());
        kernel.startGBean(wsConfgBuilderName);
        
        

    }

    protected void tearDown() throws Exception {
//        TestingUtils.stopJ2EEContinerAndAxisServlet(kernel);
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }
    
     
    
}
