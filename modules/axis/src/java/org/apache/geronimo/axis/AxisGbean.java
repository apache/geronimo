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

import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

/**
 * Class AxisGbean
 */
public class AxisGbean implements GBeanLifecycle {

    /**
     * Field name
     */
    private final String name;

    /**
     * Field kernel
     */
    private final Kernel kernel;

    /**
     * Field GBEAN_INFO
     */
    private static final GBeanInfo GBEAN_INFO;

    /**
     * Field objectName
     */
    private final ObjectName objectName;

    /**
     * Field wscontiner
     */
    private WebServiceContainer wscontiner;

    static {
        GBeanInfoFactory infoFactory = new GBeanInfoFactory("AxisGbean",
                AxisGbean.class);

        // attributes
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);

        // operations
        infoFactory.setConstructor(new String[]{"kernel", "Name",
                                                "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    /**
     * Constructor AxisGbean
     *
     * @param kernel
     * @param name
     * @param objectName
     */
    public AxisGbean(Kernel kernel, String name, String objectName) {
        this.name = name;
        this.kernel = kernel;
        this.objectName = JMXUtil.getObjectName(objectName);
        wscontiner = new WebServiceContainer(kernel);
    }

    /**
     * Method doFail
     */
    public void doFail() {
        System.out.println("Axis GBean has failed");
    }

    /**
     * Method doStart
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStart() throws WaitingException, Exception {
        System.out.println("Axis GBean has started");
        System.out.println(kernel);
        System.out.println(objectName);
        wscontiner.doStart();
    }

    /**
     * Method doStop
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStop() throws WaitingException, Exception {
        System.out.println("Axis GBean has stoped");
        wscontiner.doStop();
    }

    /**
     * Method getGBeanInfo
     *
     * @return
     */
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     * Method getKernel
     *
     * @return
     */
    public Kernel getKernel() {
        return kernel;
    }

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return name;
    }
}
