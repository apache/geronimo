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

import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.openejb.ContainerIndex;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

/**
 * Class DependancyEJBManager
 */
public class DependancyEJBManager {

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
            AxisGeronimoConstants.J2EE_SERVER_NAME;

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
    private Vector dependedEJBs;

    /**
     * Field kernel
     */
    private final Kernel kernel;

    /**
     * Constructor DependancyEJBManager
     *
     * @param kernel
     */
    public DependancyEJBManager(Kernel kernel) {
        this.kernel = kernel;
        configStore = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        dependedEJBs = new Vector();
    }

    /**
     * Method startDependancies
     *
     * @param properites
     * @throws MalformedObjectNameException
     * @throws DeploymentException
     */
    public void startDependancies(Hashtable properites)
            throws MalformedObjectNameException, DeploymentException {
        startJ2EEServer();
        startContainerIndex();

        Enumeration enu = properites.keys();

        while (enu.hasMoreElements()) {
            String dir = (String) enu.nextElement();
            String serviceName = (String) properites.get(dir);

            startDependancy(dir, serviceName);
        }
    }

    /**
     * Method stopDependancies
     *
     * @throws DeploymentException
     */
    public void stopDependancies() throws DeploymentException {
        // stop strated ejbs
        for (int i = 0; i < dependedEJBs.size(); i++) {
            AxisGeronimoUtils.stopGBean((ObjectName) dependedEJBs.get(i),
                    kernel);
        }

        // stop the continer Index
        stopContainerIndex();

        // stop the j2ee server
        stopJ2EEServer();
    }

    /**
     * Method startDependancy
     *
     * @param outDir
     * @param service
     * @throws DeploymentException
     */
    private void startDependancy(String outDir, String service)
            throws DeploymentException {
        try {
            File unpackedDir = new File(configStore, outDir);

            // load the configuration
            GBeanMBean config = loadConfig(unpackedDir);
            ObjectName objectName = ObjectName.getInstance("test:configuration="
                    + service);

            dependedEJBs.add(objectName);
            kernel.loadGBean(objectName, config);
            config.setAttribute("baseURL", unpackedDir.toURL());

            // start the configuration
            kernel.startRecursiveGBean(objectName);
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
     * Method stopCantainerIndex
     *
     * @throws DeploymentException
     */
    private void stopContainerIndex() throws DeploymentException {
        AxisGeronimoUtils.stopGBean(containerIndexObjectName, kernel);
    }

    /**
     * Method startJ2EEServer
     *
     * @throws DeploymentException
     */
    private void startJ2EEServer() throws DeploymentException {
        try {
            String str =
                    System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);

            if (str == null) {
                str = ":org.apache.geronimo.naming";
            } else {
                str = str + ":org.apache.geronimo.naming";
            }

            System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);

            GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);

            serverInfoGBean.setAttribute("baseDirectory", ".");

            this.serverInfoObjectName = ObjectName.getInstance(j2eeDomainName
                    + ":type=ServerInfo");

            AxisGeronimoUtils.startGBean(serverInfoObjectName, serverInfoGBean,
                    kernel);

            GBeanMBean j2eeServerGBean =
                    new GBeanMBean(J2EEServerImpl.GBEAN_INFO);

            j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));

            this.j2eeServerObjectName = ObjectName.getInstance(j2eeDomainName
                    + ":j2eeType=J2EEServer,name=" + j2eeServerName);

            AxisGeronimoUtils.startGBean(j2eeServerObjectName, j2eeServerGBean,
                    kernel);

            GBeanMBean tmGBean =
                    new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
            Set patterns = new HashSet();

            patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
            patterns.add(ObjectName.getInstance("geronimo.server:j2eeType=ActivationSpec,*"));
            tmGBean.setReferencePatterns("resourceManagers", patterns);
            AxisGeronimoUtils.startGBean(transactionManagerObjectName, tmGBean,
                    kernel);

            GBeanMBean connectionTrackerGBean =
                    new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
            ObjectName connectionTrackerObjectName =
                    ObjectName.getInstance(j2eeDomainName
                    + ":type=ConnectionTracker");

            AxisGeronimoUtils.startGBean(connectionTrackerObjectName,
                    connectionTrackerGBean, kernel);

            // //load mock resource adapter for mdb
            // DeploymentHelper.setUpResourceAdapter(kernel);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Method stopJ2EEServer
     *
     * @throws DeploymentException
     */
    private void stopJ2EEServer() throws DeploymentException {
        AxisGeronimoUtils.stopGBean(serverInfoObjectName, kernel);
        AxisGeronimoUtils.stopGBean(j2eeServerObjectName, kernel);
        AxisGeronimoUtils.stopGBean(transactionManagerObjectName, kernel);
        AxisGeronimoUtils.stopGBean(connectionTrackerObjectName, kernel);
    }

    /**
     * Method loadConfig
     *
     * @param unpackedCar
     * @return
     * @throws Exception
     */
    private GBeanMBean loadConfig(File unpackedCar) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar,
                "META-INF/config.ser"));

        try {
            ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(in));
            GBeanInfo gbeanInfo = Configuration.GBEAN_INFO;
            GBeanMBean config = new GBeanMBean(gbeanInfo);

            Configuration.loadGMBeanState(config, ois);
            return config;
        } finally {
            in.close();
        }
    }
}
