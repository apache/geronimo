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
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.UserTransactionImpl;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.zip.ZipFile;

/**
 * Class WebServiceContainer
 */
public class WebServiceManager {
    protected final Log log = LogFactory.getLog(getClass());
    /**
     * Field kernel
     */
    private final Kernel kernel;

    /**
     * Field containerName
     */
    private ObjectName containerName;

    /**
     * Field mbServer
     */
    private MBeanServer mbServer;

    /**
     * Field containerPatterns
     */
    private Set containerPatterns;

    /**
     * Field appName
     */
    private ObjectName appName;

    /**
     * Field ejbManager
     */
    private DependancyManager ejbManager;

    /**
     * Constructor WebServiceContainer
     *
     * @param kernel
     */
    public WebServiceManager(Kernel kernel) {
        this.kernel = kernel;
        mbServer = kernel.getMBeanServer();

        containerName = AxisGeronimoConstants.WEB_CONTAINER_NAME;
        containerPatterns = Collections.singleton(containerName);
        appName = AxisGeronimoConstants.APPLICATION_NAME;
    }

    /**
     * Method init
     */
    public void init() {
    }

    /**
     * Method doStart
     *
     * @throws Exception
     */
    public void doStart() throws Exception {
        ejbManager = new DependancyManager(kernel);
        //This code is taken from the org.apache.geronimo.jetty.ApplicationTest in the 
        //jetty module tests .. If something is not working we got to test wheather the 
        //test has changed
        ClassUtils.setDefaultClassLoader(Thread.currentThread().getContextClassLoader());
        loadDeployedWebservices();

        GBeanMBean app = new GBeanMBean("org.apache.geronimo.jetty.JettyWebAppContext");
        URL url =
                Thread.currentThread().getContextClassLoader().getResource("deployables/axis/");

        app.setAttribute("uri", URI.create(url.toString()));
        app.setAttribute("contextPath", "/axis");
        app.setAttribute("componentContext", null);
        UserTransactionImpl userTransaction = new UserTransactionImpl();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePatterns("Configuration", Collections.EMPTY_SET);
        app.setReferencePatterns("JettyContainer", containerPatterns);
        app.setReferencePattern("TransactionContextManager", AxisGeronimoConstants.TRANSACTION_CONTEXT_MANAGER_NAME);
        app.setReferencePattern("TrackedConnectionAssociator", AxisGeronimoConstants.TRACKED_CONNECTION_ASSOCIATOR_NAME);

        start(appName, app);
        log.debug("Axis started as a web application inside Jetty");
    }

    /**
     * Method doStop
     *
     * @throws Exception
     */
    public void doStop() throws Exception {
        ejbManager.stopDependancies();
    }

    /**
     * Method start
     *
     * @param name
     * @param instance
     * @throws Exception
     */
    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    /**
     * Method stop
     *
     * @param name
     * @throws Exception
     */
    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }

    /**
     * Method loadDeployedWebservices
     *
     * @throws MalformedURLException
     */
    public void loadDeployedWebservices()
            throws MalformedURLException, DeploymentException {
        try {
            File configStroe = new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);
            File[] apps = configStroe.listFiles();
            ClassLoader classloader;
            if (apps != null) {
                for (int i = 0; i < apps.length; i++) {
                    File f = new File(apps[i], "axis.properties");
                    if (f.exists()) {
                        Properties p = new Properties();
                        FileInputStream in = new FileInputStream(f);
                        p.load(in);
                        in.close();  
                        //that mean this a Web Service
                        String dir = f.getName();
                        String serviceName = (String) p.get(dir);
                        String style = (String) p.get("style");
                        File module = null;

                        File[] jars = f.listFiles();
                        if (jars != null) {
                            for (int j = 0; j < jars.length; j++) {
                                if (jars[i].getAbsolutePath().endsWith(".jar")) {
                                    module = jars[i];
                                    log.debug("found a jar" + jars[i].getAbsolutePath());
                                    break;
                                }
                            }
                        }

                        if ("ejb".equals(style)) {
                            ObjectName serviceobjectName = ObjectName.getInstance("test:configuration="
                                    + serviceName);
                            classloader = DependancyManager.startDependancy(apps[i], serviceobjectName, configStroe, kernel);
                        } else {
                            classloader = new URLClassLoader(new URL[]{module.toURL()});
                        }

                        ArrayList classList = AxisGeronimoUtils.getClassFileList(new ZipFile(module));
                        for (int j = 0; j < classList.size(); j++) {
                            String className = (String) classList.get(i);
                            System.out.println(className);
                            ClassUtils.setClassLoader(className, classloader);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }
}
