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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLClassLoader;

import javax.management.ObjectName;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
/**
 * <p> This test case start the AxisGbean and test retiving the index.html of the 
 * Axis Service</p>   
 */
public class AxisGBeanTest extends AbstractTestCase {
    private ObjectName name;
    private Kernel kernel;
    private JettyServiceWrapper jettyService;

    /**
     * @param testName
     */
    public AxisGBeanTest(String testName) {
        super(testName);
    }

    public void testStartAxisService() throws Exception {
        String textFileurl = "http://localhost:"+AxisGeronimoConstants.AXIS_SERVICE_PORT+"/axis/index.html";
        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanMBean gbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        gbean.setAttribute("Name", "Test");
        kernel.loadGBean(name, gbean);
        kernel.startGBean(name);
        System.out.println(kernel.getMBeanServer().getAttribute(name, "state"));
//        System.out.println(
//                kernel.getMBeanServer().invoke(name, "echo",
//                        new Object[]{"Hello"},
//                        new String[]{String.class.getName()}));
        HttpURLConnection connection = (HttpURLConnection) new URL(textFileurl).openConnection();
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        connection.getResponseCode();
        String line = reader.readLine();
        while (line != null) {
            System.out.println(line);
            line = reader.readLine();
        }
        connection.disconnect();
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
    }

    protected void setUp() throws Exception {
        name = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();
		jettyService = new JettyServiceWrapper(kernel);
		jettyService.doStart();
    }

    protected void tearDown() throws Exception {
		jettyService.doStop();
        kernel.shutdown();
    }
}
