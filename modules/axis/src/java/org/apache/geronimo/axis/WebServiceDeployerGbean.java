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

/**
 * Class WebServiceDeployerGbean
 */
public class WebServiceDeployerGbean implements GBeanLifecycle {

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
     * Field wsdeployer
     */
    private WebServiceDeployer wsdeployer;

    static {
        GBeanInfoFactory infoFactory =
                new GBeanInfoFactory("WebServiceDeployerGbean",
                        WebServiceDeployerGbean.class);

        // attributes
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);

        // operations
        infoFactory.addOperation("deploy", new Class[]{String.class,
                                                       String.class,
                                                       String.class});
        infoFactory.setConstructor(new String[]{"kernel", "Name",
                                                "objectName"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    /**
     * Constructor WebServiceDeployerGbean
     *
     * @param kernel
     * @param name
     */
    public WebServiceDeployerGbean(Kernel kernel, String name) {
        this.name = name;
        this.kernel = kernel;
    }

    /*
     * (non-Javadoc)
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doFail()
     */

    /**
     * Method doFail
     */
    public void doFail() {
        System.out.println("WebServiceDeployerGbean has failed");
    }

    /*
     * (non-Javadoc)
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStart()
     */

    /**
     * Method doStart
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStart() throws WaitingException, Exception {

        System.out.println("WebServiceDeployerGbean has started");

        wsdeployer = new WebServiceDeployer(AxisGeronimoConstants.TEMP_OUTPUT,
                kernel);
    }

    /*
     * (non-Javadoc)
     * @see org.apache.geronimo.gbean.GBeanLifecycle#doStop()
     */

    /**
     * Method doStop
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStop() throws WaitingException, Exception {
        System.out.println("WebServiceDeployerGbean has stoped");
    }

    /**
     * Method deploy
     *
     * @param module
     * @param j2eeApplicationName
     * @param j2eeModuleName
     * @throws Exception
     */
    public void deploy(String module, String j2eeApplicationName, String j2eeModuleName)
            throws Exception {
        wsdeployer.deploy(module, j2eeApplicationName, j2eeModuleName);
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
