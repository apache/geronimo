/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.kernel;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.List;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashSet;
import java.beans.PropertyEditorManager;
import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.NotificationListener;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.LogFactoryImpl;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.client.DeploymentNotification;
import org.apache.geronimo.kernel.jmx.JMXKernel;

/**
 * Main entry point for the Geronimo server.
 *
 * @version $Revision: 1.4 $ $Date: 2004/01/01 18:32:13 $
 */
public class Main implements Runnable {

    static String GERONIMO_HOME;

    static {
        // Add our default Commons Logger that support the trace level
        // This MUST be done before the first log is acquired
        if (System.getProperty(LogFactoryImpl.LOG_PROPERTY) == null) {
            System.setProperty(LogFactoryImpl.LOG_PROPERTY, "org.apache.geronimo.kernel.log.CachingLog4jLog");
        }

        // Set the home directory based on the start location of the Java process
        if (System.getProperty("geronimo.home") == null) {
            try {
                System.setProperty("geronimo.home", (new File("")).getAbsoluteFile().toURL().toString());
            } catch (MalformedURLException e) {
                throw new AssertionError(e);
            }
        }
        GERONIMO_HOME = System.getProperty("geronimo.home");
        
        // add our commons property editors incase the commons module is added latter
        List paths = new LinkedList(Arrays.asList(PropertyEditorManager.getEditorSearchPath()));
        paths.add("org.apache.geronimo.common.propertyeditor");
        PropertyEditorManager.setEditorSearchPath((String[])paths.toArray(new String[paths.size()]));
    }

    private static final Log log = LogFactory.getLog("Geronimo");
    private static final String[] DEPLOY_ARG_TYPES = {"java.net.URL"};

    private final String domainName;
    private final URL mletURL;
    private final URL bootURL;

    private ObjectName deployerName;

    public Main(String domainName, URL mletURL, URL bootURL) {
        this.domainName = domainName;
        this.mletURL = mletURL;
        this.bootURL = bootURL;
    }

    /**
     * Main entry point
     */
    public void run() {
        final long time = System.currentTimeMillis();

        Object[] deployArgs = {bootURL};
        JMXKernel kernel = null;
        ShutdownThread hook = new ShutdownThread("Shutdown-Thread", Thread.currentThread());
        ObjectName controllerName = null;
        try {
            Runtime.getRuntime().addShutdownHook(hook);
            try {
                log.info("Starting JMXKernel");
                kernel = new JMXKernel(domainName);

                // boot my kernel MLet
                Set bootedMBeans = kernel.bootMLet(mletURL);

                // check they all started OK and it included a controller and service planner
                ObjectName controllerPattern = new ObjectName("*:role=DeploymentController,*");
                ObjectName deployerPattern = new ObjectName("*:role=ApplicationDeployer,*");
                ObjectName plannerPattern = new ObjectName("*:role=DeploymentPlanner,type=Service,*");
                boolean planner = false;
                for (Iterator i = bootedMBeans.iterator(); i.hasNext();) {
                    Object o = i.next();
                    if (o instanceof Throwable) {
                        throw (Throwable) o;
                    }
                    ObjectName mletName = ((ObjectInstance) o).getObjectName();
                    if (controllerPattern.apply(mletName)) {
                        if (controllerName != null) {
                            throw new DeploymentException("Multiple DeploymentControllers specified in boot mlet");
                        }
                        controllerName = mletName;
                    } else if (plannerPattern.apply(mletName)) {
                        planner = true;
                    } else if(deployerPattern.apply(mletName)) {
                        if(deployerName != null) {
                            throw new DeploymentException("Multiple ApplicationDeployers specified in boot mlet");
                        }
                        deployerName = mletName;
                    }
                }
                if (controllerName == null) {
                    throw new DeploymentException("Boot mlet did not load a DeploymentController");
                } else if (deployerName == null) {
                        throw new DeploymentException("Boot mlet did not load an ApplicationDeployer");
                } else if (!planner) {
                    throw new DeploymentException("Boot mlet did not load a DeploymentPlanner for type=Service");
                }

                // start her up
                log.info("Deploying Bootstrap Services from " + bootURL);
                final MBeanServer mbServer = kernel.getMBeanServer();
                final ObjectName nameToDeregister = controllerName;
                mbServer.addNotificationListener(controllerName, new NotificationListener() {
                    private Set ids = new HashSet();
                    private int zeroCount = 0;
                    public void handleNotification(Notification notification, Object handback) {
                        DeploymentNotification dn = (DeploymentNotification)notification;
                        if(dn.getType().equals(DeploymentNotification.DEPLOYMENT_UPDATE)) {
                            ids.add(dn.getTargetModuleID());
                        } else {
                            ids.remove(dn.getTargetModuleID());
                        }
                        if(ids.size() == 0) {
                            ++zeroCount;
                            if(zeroCount > 1) { // todo: here we assume that the first run of the DeploymentScanner will not do anything.  We could save several seconds if we fixed that, but for now this works.
                                // Booted... print the startup time
                                long delta = (System.currentTimeMillis() - time) / 1000;
                                StringBuffer startMessage = new StringBuffer(50);
                                startMessage.append("Started Server in ");
                                if(delta > 60) {
                                    startMessage.append(delta / 60).append("m ");
                                }
                                startMessage.append(delta %  60).append("s");
                                log.info(startMessage);
                                try {
                                    mbServer.removeNotificationListener(nameToDeregister, this);
                                } catch(InstanceNotFoundException e) {
                                    log.error("Couldn't remove start listener", e);
                                } catch(ListenerNotFoundException e) {
                                    log.error("Couldn't remove start listener", e);
                                }
                            }
                        }
                    }
                }, new NotificationFilter() {
                    public boolean isNotificationEnabled(Notification notification) {
                        return true;
                    }
                }, null);
                mbServer.invoke(deployerName, "deploy", deployArgs, DEPLOY_ARG_TYPES);

            } catch (Throwable e) {
                log.error("Error starting Server", e);
                return;
            }

            // and now go to sleep until we get interrupted
            try {
                // loop forever to avoid exiting on suprious notify
                while (true) {
                    synchronized (this) {
                        wait();
                    }
                }
            } catch (InterruptedException e) {
                // time to go...
            }
        } finally {
            try {
                Runtime.getRuntime().removeShutdownHook(hook);
            } catch (Exception e) {
                // we were in the process of shutting down - ignore
            }
            if (kernel != null) {
                if (deployerName != null) {
                    try {
                        log.info("Undeploy Bootstrap Services");
                        MBeanServer mbServer = kernel.getMBeanServer();
                        mbServer.invoke(deployerName, "undeploy", deployArgs, DEPLOY_ARG_TYPES);
                    } catch (Throwable e) {
                        log.error("Error stopping Server", e);
                    }
                }

                log.info("Releasing JMXKernel");
                kernel.release();
            }

            log.info("Shutdown complete");
        }
    }

    /**
     * Command line entry point.
     * Starts a new ThreadGroup so that all owned threads can be identified.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        try {
            // @todo get these from somewhere a little more flexible
            URL homeURL = new URL(GERONIMO_HOME);
            URL mletURL = new URL(homeURL, "etc/boot.mlet");
            URL deployURL = new URL(homeURL, "etc/boot-service.xml");

            log.info("Geronimo Home: " + System.getProperty("geronimo.home"));
            log.info("Bootstrap URL: " + mletURL);
            log.info("Boot services URL: " + deployURL);
            log.info("Log Implementation: " + System.getProperty(LogFactoryImpl.LOG_PROPERTY));
            Main main = new Main("geronimo", mletURL, deployURL);

            ThreadGroup group = new ThreadGroup("Geronimo");
            Thread mainThread = new Thread(group, main, "Main-Thread");
            mainThread.start();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
    }

    private static class ShutdownThread extends Thread {
        private final Thread mainThread;

        private ShutdownThread(String name, Thread mainThread) {
            super(name);
            this.mainThread = mainThread;
        }

        public void run() {
            mainThread.interrupt();
        }
    }
}
