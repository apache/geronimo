/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.axis;

import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;

import javax.management.ObjectName;
import java.net.URL;
import java.net.URLClassLoader;
/**
 * <p>Simple stanalone Axis Service started via a GBean. This is a test utility only</p>  
 * @author hemapani@opensource.lk
 */

public class TestServer {
    private Kernel kernel;
    private ObjectName name;
	private JettyServiceWrapper jettyService;

    public TestServer() throws Exception {
        name = new ObjectName("test:name=AxisGBean");
        kernel = new Kernel("test.kernel", "test");
        kernel.boot();

		jettyService = new JettyServiceWrapper(kernel);
		jettyService.doStart();

        ClassLoader cl = getClass().getClassLoader();
        ClassLoader myCl = new URLClassLoader(new URL[0], cl);
        GBeanMBean gbean = new GBeanMBean(AxisGbean.getGBeanInfo(), myCl);
        gbean.setAttribute("Name", "Test");
        kernel.loadGBean(name, gbean);
        kernel.startGBean(name);
        System.in.read();
        System.out.println("Shutting down the kernel");
        kernel.stopGBean(name);
        kernel.unloadGBean(name);
        
		jettyService.doStop();
        kernel.shutdown();
    }

}
