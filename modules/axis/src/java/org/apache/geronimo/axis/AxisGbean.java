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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
    private static Log log = LogFactory.getLog(AxisGbean.class);
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
    private WebServiceManager wscontiner;

    private WebServiceDeployer wsdeployer;

    private J2EEManager j2eeManager;

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
        infoFactory.addOperation("deploy", new Class[]{String.class,
                                                       String.class,
                                                       String.class});
        infoFactory.addOperation("unDeploy", new Class[]{String.class,
                                                         String.class});
        infoFactory.addOperation("deployEWSModule", new Class[]{String.class,
                                                                String.class,
                                                                String.class});
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
        wscontiner = new WebServiceManager(kernel);
        j2eeManager = new J2EEManager();
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
        log.info(kernel);
        log.info(objectName);
        j2eeManager.startJ2EEContainer(kernel);
        wscontiner.doStart();
        wsdeployer = new WebServiceDeployer(AxisGeronimoConstants.TEMP_OUTPUT,
                kernel);

    }

    /**
     * Method doStop
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStop() throws WaitingException, Exception {
        log.info("Axis GBean has stoped");
        wscontiner.doStop();
        j2eeManager.stopJ2EEContainer(kernel);
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

    public void deploy(String module, String j2eeApplicationName, String j2eeModuleName)
            throws Exception {
        wsdeployer.deploy(module, j2eeApplicationName, j2eeModuleName);
    }

    public void deployEWSModule(String module, String j2eeApplicationName, String j2eeModuleName)
            throws Exception {
        wsdeployer.deployEWSModule(module, j2eeApplicationName, j2eeModuleName);
    }

    public void unDeploy(String j2eeApplicationName, String j2eeModuleName)
            throws Exception {
        wsdeployer.unDeploy(j2eeApplicationName, j2eeModuleName);
    }

}
