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
import javax.management.ObjectName;

import org.apache.geronimo.axis.testUtils.AxisGeronimoConstants;
import org.apache.geronimo.axis.testUtils.J2EEManager;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

public class WSConfigBuilderTest extends AbstractTestCase {
    private ObjectName configBuilderName;
    private Kernel kernel;
    private J2EEManager j2eeManager;

    /**
     * @param testName
     */
    public WSConfigBuilderTest(String testName) {
        super(testName);
        j2eeManager = new J2EEManager();
    }

    public void testLoad() throws Exception {
        kernel.getConfigurationManager().load(new File("modules/axis/test-resources/plans/plan1.xml").toURI());
        //axis gbean        
        GBeanMBean axisgbean = new GBeanMBean(WSConfigBuilder.getGBeanInfo());
        kernel.loadGBean(configBuilderName, axisgbean);
        kernel.startGBean(configBuilderName);
        kernel.stopGBean(configBuilderName);
        kernel.unloadGBean(configBuilderName);
    }

    protected void setUp() throws Exception {
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        configBuilderName = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
        AxisGeronimoUtils.delete(file);
        file.getParentFile().mkdirs();
        j2eeManager.startJ2EEContainer(kernel);
    }

    protected void tearDown() throws Exception {
        j2eeManager.stopJ2EEContainer(kernel);
        kernel.shutdown();
        File file = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        AxisGeronimoUtils.delete(file);
    }

}
