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
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.jetty.JettyWebAppContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.transaction.UserTransactionImpl;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.File;
import java.io.FileInputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

/**
 * Class WebServiceContainer
 */
public class WebServiceContainer {
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
     * Field tmName
     */
    private ObjectName tmName;

    /**
     * Field tcaName
     */
    private ObjectName tcaName;

    /**
     * Field appName
     */
    private ObjectName appName;

    /**
     * Field ejbManager
     */
    private DependancyEJBManager ejbManager;
    
    private ObjectName tcmName;

    /**
     * Constructor WebServiceContainer
     *
     * @param kernel
     */
    public WebServiceContainer(Kernel kernel) {
        try {
            this.kernel = kernel;
            mbServer = kernel.getMBeanServer();

            // get refernace to the Jetty web continer artifacts
            // TODO check they are same names for all the time
            containerName =
                    new ObjectName(AxisGeronimoConstants.WEB_CONTANER_NAME);
            containerPatterns = Collections.singleton(containerName);
            appName =
                    new ObjectName(AxisGeronimoConstants.APPLICATION_NAME);
            tmName = new ObjectName(AxisGeronimoConstants.TRANSACTION_MANAGER_NAME);
            tcmName = new ObjectName(AxisGeronimoConstants.TRANSACTION_CONTEXT_MANAGER_NAME);
            tcaName = new ObjectName(AxisGeronimoConstants.CONNTECTION_TRACKING_COORDINATOR);
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException(e);
        }
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
        ejbManager = new DependancyEJBManager(kernel);
        
        //The code needed a axis.properties file. If it is not there it is created.
        //the ideal case is this information should be in the server-config.wsdd file
        //but still the Axis is stanalone. I do not like the idea of yet another 
        //deployment discrypter.    
        File axisPopertiesfile =
                new File(new File(AxisGeronimoConstants.AXIS_CONFIG_STORE),
                        "axis.properties");
        Properties axisProperties = new Properties();

        if (axisPopertiesfile.exists()) {
            axisProperties.load(new FileInputStream(axisPopertiesfile));
            log.debug(axisPopertiesfile.getAbsoluteFile() + " file found and loaded");
        } else {
            axisPopertiesfile.getParentFile().mkdirs();
            axisPopertiesfile.createNewFile();
            log.debug(" axis.properties file not found and created");
        }

        // TODO deployed webservices should be stored in the local config store
        // This is a hack till it is found out how to do it
        ClassLoader myCl =
                new URLClassLoader(loadDeployedWebservices(axisProperties));

        // Start the EJB's that depend on the webservices
        ejbManager.startDependancies(axisProperties);
        log.debug("start dependent EJBs ");
        
        //This code is taken from the org.apache.geronimo.jetty.ApplicationTest in the 
        //jetty module tests .. If something is not working we got to test weather the 
        //test has changed
        
        GBeanMBean app = new GBeanMBean(JettyWebAppContext.GBEAN_INFO, myCl);
        URL url =
                Thread.currentThread().getContextClassLoader().getResource("deployables/axis/");

//        app.setAttribute("uri", URI.create(url.toString()));
//        app.setAttribute("contextPath", "/axis");
//        app.setAttribute("componentContext", null);
//        UserTransactionImpl userTransaction = new UserTransactionImpl();
//        app.setAttribute("userTransaction", userTransaction);
//        app.setReferencePatterns("Configuration", Collections.EMPTY_SET);
//        app.setReferencePatterns("JettyContainer", containerPatterns);
//        app.setReferencePatterns("TransactionContextManager",
//                Collections.singleton(tmName));
//        app.setReferencePatterns("TrackedConnectionAssociator",
//                Collections.singleton(tcaName));
                
        app.setAttribute("uri", URI.create(url.toString()));
        app.setAttribute("contextPath", "/axis");
        app.setAttribute("componentContext", null);
        UserTransactionImpl userTransaction = new UserTransactionImpl();
        app.setAttribute("userTransaction", userTransaction);
        app.setReferencePatterns("Configuration", Collections.EMPTY_SET);
        app.setReferencePatterns("JettyContainer", containerPatterns);
        app.setReferencePattern("TransactionContextManager", tcmName);
        app.setReferencePattern("TrackedConnectionAssociator", tcaName);


        // TODO add a dependancy to such that to this service to started the
        // jetty must have been started
        start(appName, app);
        log.debug("Axis started as a web application inside Jetty");
    }

    /**
     * Method doStop
     *
     * @throws Exception
     */
    public void doStop() throws Exception {
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
     * @param properties
     * @return
     * @throws MalformedURLException
     */
    public URL[] loadDeployedWebservices(Hashtable properties)
            throws MalformedURLException {
        if (properties == null) {
            return new URL[0];
        }

        Vector urls = new Vector();
        Enumeration enu = properties.keys();
        File configStroe =
                new File(AxisGeronimoConstants.AXIS_CONFIG_STORE);

        while (enu.hasMoreElements()) {
            File libfile = new File(configStroe, enu.nextElement().toString());
            if (libfile.exists()) {
                File[] jars = libfile.listFiles();
                if (jars != null) {
                    for (int i = 0; i < jars.length; i++) {
                        if (jars[i].getAbsolutePath().endsWith(".jar")) {
                            urls.add(jars[i].toURL());
                            log.debug("found a jar" + jars[i].getAbsolutePath());
                        }
                    }
                }
            }
        }

        URL[] urlList = new URL[urls.size()];
        for (int i = 0; i < urls.size(); i++) {
            urlList[i] = (URL) urls.get(i);
            System.out.println(urlList[i]);
        }
        return urlList;
    }
}
