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

import org.apache.axis.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.openejb.ContainerIndex;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Class DependancyEJBManager
 */
public class DependancyManager {
    protected final Log log = LogFactory.getLog(getClass());
    /**
     * Field j2eeServerObjectName
     */
    private ObjectName j2eeServerObjectName;

    /**
     * Field j2eeDomainName
     */
    private static final String j2eeDomainName =
            AxisGeronimoConstants.J2EE_DOMAIN_NAME;

    /**
     * Field j2eeServerName
     */
    private static final String j2eeServerName =
            AxisGeronimoConstants.J2EE_SERVER_PREFIX;

    /**
     * Field transactionManagerObjectName
     */
    private static final ObjectName transactionManagerObjectName =
            JMXUtil.getObjectName(j2eeDomainName + ":type=TransactionManager");

    /**
     * Field connectionTrackerObjectName
     */
    private static final ObjectName connectionTrackerObjectName =
            JMXUtil.getObjectName(j2eeDomainName + ":type=ConnectionTracker");

    /**
     * Field containerIndexObjectName
     */
    private ObjectName containerIndexObjectName;

    /**
     * Field serverInfoObjectName
     */
    private ObjectName serverInfoObjectName;

    /**
     * Field configStore
     */
    private File configStore;

    /**
     * Field dependedEJBs
     */
    private static Vector dependedEJBs;

    /**
     * Field kernel
     */
    private final Kernel kernel;

    /**
     * Constructor DependancyEJBManager
     *
     * @param kernel
     */
    public DependancyManager(Kernel kernel) throws MalformedObjectNameException, DeploymentException {
        this.kernel = kernel;
        configStore = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        log.debug("configuration Store is " + configStore.getAbsolutePath());
        dependedEJBs = new Vector();
    }

    /**
     * Method stopDependancies
     *
     * @throws DeploymentException
     */
    public void stopDependancies() throws DeploymentException {
        // stop strated ejbs
        for (int i = 0; i < dependedEJBs.size(); i++) {
            ObjectName gbeanName = (ObjectName) dependedEJBs.get(i);
            AxisGeronimoUtils.stopGBean(gbeanName, kernel);
            System.out.println("stop the dependent EJB name=" + gbeanName);
            log.debug("stop the dependent EJB name=" + gbeanName);
        }
    }

    /**
     * Method startDependancy
     *
     * @param unpackedDir
     * @param service
     * @throws DeploymentException
     */
    public static ClassLoader startDependancy(File unpackedDir,
                                              ObjectName service,
                                              File configStore,
                                              Kernel kernel)
            throws DeploymentException {
        try {
            // load the configuration
            GBeanMBean config = loadConfig(unpackedDir);
            System.out.println("Context Loader " + Thread.currentThread().getContextClassLoader());
            ObjectName objectName = service;

            dependedEJBs.add(objectName);
            kernel.loadGBean(objectName, config);
            config.setAttribute("baseURL", unpackedDir.toURL());

            // start the configuration
            kernel.startRecursiveGBean(objectName);

            ClassLoader cl = (ClassLoader) kernel.getAttribute(objectName, "classLoader");

            System.out.println("start dependent EJB name=" + objectName
                    + " dir=" + unpackedDir.getAbsolutePath() + "the config CL =" + cl);
            return cl;
        } catch (DeploymentException e) {
            throw e;
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method startContainerIndex
     *
     * @throws DeploymentException
     * @throws MalformedObjectNameException
     */
    private void startContainerIndex()
            throws DeploymentException, MalformedObjectNameException {
        GBeanMBean containerIndexGBean =
                new GBeanMBean(ContainerIndex.GBEAN_INFO);

        containerIndexObjectName = ObjectName.getInstance(j2eeDomainName
                + ":type=ContainerIndex");

        Set ejbContainerNames = new HashSet();

        ejbContainerNames.add(ObjectName.getInstance(j2eeDomainName
                + ":j2eeType=StatelessSessionBean,*"));
        ejbContainerNames.add(ObjectName.getInstance(j2eeDomainName
                + ":j2eeType=StatefulSessionBean,*"));
        ejbContainerNames.add(ObjectName.getInstance(j2eeDomainName
                + ":j2eeType=EntityBean,*"));
        containerIndexGBean.setReferencePatterns("EJBContainers",
                ejbContainerNames);
        AxisGeronimoUtils.startGBean(containerIndexObjectName,
                containerIndexGBean, kernel);
    }

    /**
     * Method loadConfig
     *
     * @param unpackedCar
     * @return
     * @throws Exception
     */
    private static GBeanMBean loadConfig(File unpackedCar) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar,
                "META-INF/config.ser"));

        try {
            ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(in));
            GBeanInfo gbeanInfo = Configuration.GBEAN_INFO;
            GBeanMBean config = new GBeanMBean(gbeanInfo, ClassUtils.getDefaultClassLoader());
            Configuration.loadGMBeanState(config, ois);
            return config;
        } finally {
            in.close();
        }
    }

    public static void addDependentEJB(ObjectName serviceName) {
        dependedEJBs.add(serviceName);
    }
}
