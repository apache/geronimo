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
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.axis.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.ActivationSpecWrapper;
import org.apache.geronimo.connector.ResourceAdapterWrapper;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.deployment.DeploymentException;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.j2ee.management.impl.J2EEServerImpl;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.pool.ThreadPool;
import org.apache.geronimo.system.serverinfo.ServerInfo;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledNonTransactionalTimer;
import org.apache.geronimo.timer.vm.VMStoreThreadPooledTransactionalTimer;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.openejb.ContainerIndex;

/**
 * Class DependancyEJBManager
 */
public class DependancyEJBManager {
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
    public DependancyEJBManager(Kernel kernel) throws MalformedObjectNameException, DeploymentException{
        this.kernel = kernel;
        configStore = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
        log.debug("configuration Store is "+configStore.getAbsolutePath());
        dependedEJBs = new Vector();
        //start the J2EE server        
        startJ2EEServer();
        log.debug("start the J2ee server");
        //start the ContinerIndex
        startContainerIndex();
        log.debug("start the Continer Index");
    }

    /**
     * Method startDependancies
     *
     * @param properites
     * @throws MalformedObjectNameException
     * @throws DeploymentException
     */
//    public void startDependancies(Hashtable properites)
//            throws MalformedObjectNameException, DeploymentException {
//        
//        //start the each dependent EJB
//        Enumeration enu = properites.keys();
//        while (enu.hasMoreElements()) {
//            String dir = (String) enu.nextElement();
//            String serviceName = (String) properites.get(dir);
//            ObjectName serviceobjectName = ObjectName.getInstance("test:configuration="
//                    + serviceName);
//            String wsimpl = (String)properites.get("impl");
//            File unpackedDir = new File(configStore, dir);
//            startDependancy(unpackedDir, serviceobjectName,configStore,kernel,wsimpl);
//        }
//    }

    /**
     * Method stopDependancies
     *
     * @throws DeploymentException
     */
    public void stopDependancies() throws DeploymentException {
        // stop strated ejbs
        for (int i = 0; i < dependedEJBs.size(); i++) {
            ObjectName gbeanName = (ObjectName) dependedEJBs.get(i);
            AxisGeronimoUtils.stopGBean(gbeanName,kernel);
            System.out.println("stop the dependent EJB name="+gbeanName);
            log.debug("stop the dependent EJB name="+gbeanName);
        }

        // stop the continer Index
        stopContainerIndex();
        log.debug("stop the Continer Index");
        // stop the j2ee server
        stopJ2EEServer();
        log.debug("stop the J2EE server");
    }

    /**
     * Method startDependancy
     *
     * @param outDir
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
            System.out.println("Context Loader "+Thread.currentThread().getContextClassLoader());
            ObjectName objectName = service;

            dependedEJBs.add(objectName);
            kernel.loadGBean(objectName, config);
            config.setAttribute("baseURL", unpackedDir.toURL());

            // start the configuration
            kernel.startRecursiveGBean(objectName);
            
            ClassLoader cl = (ClassLoader)kernel.getAttribute(objectName,"classLoader");
            //ClassUtils.setClassLoader(wsimpl,cl);
            //ClassUtils.setDefaultClassLoader(cl);
            
            System.out.println("start dependent EJB name="+objectName
            +" dir="+unpackedDir.getAbsolutePath()+ "the config CL ="+cl);
            return cl;
        } catch (DeploymentException e) {
           throw e;
        }catch (Exception e) {
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

            setUpTransactionManager(kernel);
            setUpTimer(kernel);

            GBeanMBean serverInfoGBean = new GBeanMBean(ServerInfo.GBEAN_INFO);
            serverInfoGBean.setAttribute("baseDirectory", ".");
            ObjectName serverInfoObjectName = ObjectName.getInstance(j2eeDomainName + ":type=ServerInfo");
            kernel.loadGBean(serverInfoObjectName, serverInfoGBean);
            kernel.startGBean(serverInfoObjectName);
            
            GBeanMBean j2eeServerGBean = new GBeanMBean(J2EEServerImpl.GBEAN_INFO);
            j2eeServerGBean.setReferencePatterns("ServerInfo", Collections.singleton(serverInfoObjectName));
            ObjectName j2eeServerObjectName = ObjectName.getInstance(AxisGeronimoConstants.J2EE_SERVER_OBJECT_NAME);
            kernel.loadGBean(j2eeServerObjectName, j2eeServerGBean);
            kernel.startGBean(j2eeServerObjectName);
                    

            // //load mock resource adapter for mdb
           // setUpResourceAdapter(kernel);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
    
    
    private void setUpTransactionManager(Kernel kernel) throws DeploymentException{
        try {
            GBeanMBean tmGBean = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
            Set rmpatterns = new HashSet();
            rmpatterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
            tmGBean.setReferencePatterns("ResourceManagers", rmpatterns);
            AxisGeronimoUtils.startGBean(AxisGeronimoConstants.TRANSACTIONMANAGER_NAME, tmGBean,kernel);
            GBeanMBean tcmGBean = new GBeanMBean(TransactionContextManager.GBEAN_INFO);
            tcmGBean.setReferencePattern("TransactionManager", AxisGeronimoConstants.TRANSACTIONMANAGER_NAME);
            AxisGeronimoUtils.startGBean(AxisGeronimoConstants.TRANSACTIONCONTEXTMANAGER_NAME, tcmGBean,kernel);
            GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
            AxisGeronimoUtils.startGBean(AxisGeronimoConstants.TRACKEDCONNECTIONASSOCIATOR_NAME, trackedConnectionAssociator,kernel);
        } catch (Exception e) {
            throw new DeploymentException(e);
        } 
    }
    
    public static void setUpTimer(Kernel kernel) throws Exception {
            GBeanMBean threadPoolGBean = new GBeanMBean(ThreadPool.GBEAN_INFO);
            threadPoolGBean.setAttribute("keepAliveTime", new Integer(5000));
            threadPoolGBean.setAttribute("poolSize", new Integer(5));
            threadPoolGBean.setAttribute("poolName", "DefaultThreadPool");
            AxisGeronimoUtils.startGBean(AxisGeronimoConstants.THREADPOOL_NAME, threadPoolGBean,kernel);
            GBeanMBean transactionalTimerGBean = new GBeanMBean(VMStoreThreadPooledTransactionalTimer.GBEAN_INFO);
            transactionalTimerGBean.setAttribute("repeatCount", new Integer(5));
            transactionalTimerGBean.setReferencePattern("TransactionContextManager", AxisGeronimoConstants.TRANSACTIONCONTEXTMANAGER_NAME);
            transactionalTimerGBean.setReferencePattern("ThreadPool", AxisGeronimoConstants.THREADPOOL_NAME);
            AxisGeronimoUtils.startGBean(AxisGeronimoConstants.TRANSACTIONALTIMER_NAME, transactionalTimerGBean,kernel);
            GBeanMBean nonTransactionalTimerGBean = new GBeanMBean(VMStoreThreadPooledNonTransactionalTimer.GBEAN_INFO);
            nonTransactionalTimerGBean.setReferencePattern("ThreadPool", AxisGeronimoConstants.THREADPOOL_NAME);
            AxisGeronimoUtils.startGBean(AxisGeronimoConstants.NONTRANSACTIONALTIMER_NAME, nonTransactionalTimerGBean,kernel);
        }

    public void setUpResourceAdapter(Kernel kernel) throws Exception {
        GBeanMBean geronimoWorkManagerGBean = new GBeanMBean(GeronimoWorkManager.getGBeanInfo());
        geronimoWorkManagerGBean.setAttribute("syncMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setAttribute("startMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setAttribute("scheduledMaximumPoolSize", new Integer(5));
        geronimoWorkManagerGBean.setReferencePattern("XAServices", AxisGeronimoConstants.TRANSACTIONMANAGER_NAME);
        AxisGeronimoUtils.startGBean(AxisGeronimoConstants.WORKMANAGER_NAME, geronimoWorkManagerGBean,kernel);

        GBeanMBean resourceAdapterGBean = new GBeanMBean(ResourceAdapterWrapper.getGBeanInfo());
        Map activationSpecInfoMap = new HashMap();
//        ActivationSpecInfo activationSpecInfo = new ActivationSpecInfo(MockActivationSpec.class, ActivationSpecWrapper.getGBeanInfo());
//        activationSpecInfoMap.put(MockActivationSpec.class.getName(), activationSpecInfo);
//        resourceAdapterGBean.setAttribute("resourceAdapterClass", MockResourceAdapter.class);
//        resourceAdapterGBean.setAttribute("activationSpecInfoMap", activationSpecInfoMap);
        resourceAdapterGBean.setReferencePattern("WorkManager", AxisGeronimoConstants.WORKMANAGER_NAME);
        AxisGeronimoUtils.startGBean(AxisGeronimoConstants.RESOURCE_ADAPTER_NAME, resourceAdapterGBean,kernel);

        GBeanMBean activationSpecGBean = new GBeanMBean(ActivationSpecWrapper.getGBeanInfo());
//        activationSpecGBean.setAttribute("activationSpecClass", MockActivationSpec.class);
        activationSpecGBean.setAttribute("containerId", AxisGeronimoConstants.CONTAINER_NAME.getCanonicalName());
        activationSpecGBean.setReferencePattern("ResourceAdapterWrapper", AxisGeronimoConstants.RESOURCE_ADAPTER_NAME);
        AxisGeronimoUtils.startGBean(AxisGeronimoConstants.ACTIVATIONSPEC_NAME, activationSpecGBean,kernel);
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
    private static GBeanMBean loadConfig(File unpackedCar) throws Exception {
        InputStream in = new FileInputStream(new File(unpackedCar,
                "META-INF/config.ser"));

        try {
            ObjectInputStream ois =
                    new ObjectInputStream(new BufferedInputStream(in));
            GBeanInfo gbeanInfo = Configuration.GBEAN_INFO;
            GBeanMBean config = new GBeanMBean(gbeanInfo,ClassUtils.getDefaultClassLoader());
            Configuration.loadGMBeanState(config, ois);
            return config;
        } finally {
            in.close();
        }
    }
    
    public static void addDependentEJB(ObjectName serviceName){
        dependedEJBs.add(serviceName);
    }
}
