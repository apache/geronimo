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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

import javax.management.ObjectName;

public class AxisGbean implements GBeanLifecycle {
    private final String name;
    private final Kernel kernel;
    private static final GBeanInfo GBEAN_INFO;
    private final ObjectName objectName;

    private WebServiceContainer wscontiner;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("AxisGbean",
                AxisGbean.class);
        //attributes
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        //operations
        infoFactory.addOperation("echo", new Class[]{String.class});
        infoFactory.setConstructor(new String[]{"kernel", "Name", "objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public AxisGbean(Kernel kernel, String name, String objectName) {
        this.name = name;
        this.kernel = kernel;
        this.objectName = JMXUtil.getObjectName(objectName);
        wscontiner = new WebServiceContainer(kernel, this.objectName);
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doFail()
     */
    public void doFail() {
        System.out.println("Axis GBean has failed");
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStart()
     */
    public void doStart() throws WaitingException, Exception {
        System.out.println("Axis GBean has started");
        System.out.println(kernel);
        System.out.println(objectName);
        wscontiner.doStart();
    }

    /* (non-Javadoc)
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStop()
     */
    public void doStop() throws WaitingException, Exception {
        System.out.println("Axis GBean has stoped");
        wscontiner.doStop();
    }

    public String echo(String msg) {
        return msg;
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    public Kernel getKernel() {
        return kernel;
    }

    public String getName() {
        return name;
    }
}
