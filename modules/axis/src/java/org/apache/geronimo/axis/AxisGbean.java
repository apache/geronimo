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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;

import javax.management.ObjectName;
import java.util.Collection;

/**
 * Class AxisGbean
 */
public class AxisGbean implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(AxisGbean.class);
    private final String name;
    private static final GBeanInfo GBEAN_INFO;
    private final ObjectName objectName;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("AxisGbean",
                AxisGbean.class);

        // attributes
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addReference("resourceManagers", Object.class);

        // operations
        infoFactory.setConstructor(new String[]{"kernel", "Name",
                                                "objectName", "resourceManagers"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    /**
     * Constructor AxisGbean
     *
     * @param kernel
     * @param name
     * @param objectName
     */
    public AxisGbean(Kernel kernel, String name, String objectName, Collection resourceManagers) {
        this.name = name;
        this.objectName = JMXUtil.getObjectName(objectName);
    }

    /**
     * Method doFail
     */
    public void doFail() {
        log.info("Axis GBean has failed");
    }

    /**
     * Method doStart
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStart() throws WaitingException, Exception {
        log.info("Axis GBean has started");
        log.info(objectName);
    }

    /**
     * Method doStop
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStop() throws WaitingException, Exception {
        log.info("Axis GBean has stoped");
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
     * Method getName
     *
     * @return
     */
    public String getName() {
        return name;
    }
}
