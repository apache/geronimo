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
package org.apache.geronimo.axis.preconditions;

import org.apache.geronimo.axis.AbstractTestCase;
import org.apache.geronimo.axis.AxisGbean;
import org.apache.geronimo.axis.AxisGeronimoUtils;
import org.apache.geronimo.axis.ReferenceCollectionImpl;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

import javax.management.ObjectName;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * <p> This test case start the AxisGbean and test retiving the index.html of the
 * Axis Service</p>
 */
public class GBeanConfigTest extends AbstractTestCase {
    private ObjectName name1;
    private ObjectName name2;
    private Kernel kernel;

    /**
     * @param testName
     */
    public GBeanConfigTest(String testName) {
        super(testName);
    }

    public void testStartAxisService() throws Exception {
        String textFileurl = "http://localhost:" + AxisGeronimoUtils.AXIS_SERVICE_PORT + "/axis/index.html";
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        ReferenceCollection rc = new ReferenceCollectionImpl();
        GBeanMBean gbean2 = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        gbean2.setAttribute("Name", "Test");
        kernel.loadGBean(name2, gbean2);
        kernel.startGBean(name2);

        kernel.stopGBean(name2);
        kernel.unloadGBean(name2);
        kernel.stopGBean(name1);
        kernel.unloadGBean(name1);
    }

    public void testdependencies() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        ReferenceCollection rc = new ReferenceCollectionImpl();
        GBeanMBean gbean2 = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        gbean2.setAttribute("Name", "Test");
        kernel.loadGBean(name2, gbean2);
        kernel.startGBean(name2);

        kernel.stopGBean(name2);
        kernel.unloadGBean(name2);
        kernel.stopGBean(name1);
        kernel.unloadGBean(name1);
    }

    protected void setUp() throws Exception {
        name1 = new ObjectName("test:name=WebServiceGBean");
        name2 = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }
}
