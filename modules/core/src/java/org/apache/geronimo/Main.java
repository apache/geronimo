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
package org.apache.geronimo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.Properties;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.loading.MLet;

import org.apache.geronimo.deployment.DeploymentException;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;

/**
 *
 *
 *
 * @version $Revision: 1.4 $ $Date: 2003/08/11 21:04:56 $
 */
public class Main implements Runnable {
    static {
        // Add our default Commons Logger that support the trace level
        if(System.getProperty(LogFactoryImpl.LOG_PROPERTY) == null) {
            System.setProperty(LogFactoryImpl.LOG_PROPERTY, "org.apache.geronimo.core.log.Log4jLog");
        }
    }
    private static final Log log = LogFactory.getLog("Geronimo");
    private static final String[] DEPLOY_ARG_TYPES = {"java.net.URL"};

    private final String domainName;
    private final URL mletURL;
    private final URL bootURL;

    private MBeanServer mbServer;
    private ObjectName serverName;
    private ObjectName bootMLetName;
    private Set bootedMBeans;

    public Main(String domainName, URL mletURL, URL bootURL) {
        this.domainName = domainName;
        this.mletURL = mletURL;
        this.bootURL = bootURL;
    }

    /**
     * Main entry point
     */
    public void run() {
        Object[] deployArgs = {bootURL};
        ShutdownThread hook = new ShutdownThread("Shutdown-Thread", Thread.currentThread());
        try {
            Runtime.getRuntime().addShutdownHook(hook);
            try {
                long start = System.currentTimeMillis();

                log.info("Starting MBeanServer");
                mbServer = MBeanServerFactory.createMBeanServer(domainName);

                String urlString = mletURL.toString();
                log.info("Booting MLets from URL " + urlString);

                MLet bootMLet = new MLet();
                bootMLetName = mbServer.registerMBean(bootMLet, new ObjectName("geronimo.boot:type=BootMLet,bootURL=" + ObjectName.quote(urlString))).getObjectName();
                bootedMBeans = bootMLet.getMBeansFromURL(mletURL);

                // check they all loaded OK
                ObjectName serverPattern = new ObjectName("*:role=DeploymentController,*");
                for (Iterator i = bootedMBeans.iterator(); i.hasNext();) {
                    Object o = i.next();
                    if (o instanceof Throwable) {
                        throw (Throwable) o;
                    }
                    ObjectName mletName = ((ObjectInstance) o).getObjectName();
                    if (serverPattern.apply(mletName)) {
                        if (serverName != null) {
                            throw new DeploymentException("Multiple DeploymentControllers specified in boot mlet");
                        }
                        serverName = mletName;
                    }
                }

                // start her up
                log.info("Deploying Bootstrap Services from " + bootURL);
                mbServer.invoke(serverName, "deploy", deployArgs, DEPLOY_ARG_TYPES);

                long end = System.currentTimeMillis();
                log.info("Started Server in " + (end - start) + "ms.");
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
            if (serverName != null) {
                try {
                    log.info("Undeploy Bootstrap Services");
                    mbServer.invoke(serverName, "undeploy", deployArgs, DEPLOY_ARG_TYPES);
                } catch (Throwable e) {
                    log.error("Error stopping Server", e);
                }
            }

            if (bootedMBeans != null) {
                for (Iterator i = bootedMBeans.iterator(); i.hasNext();) {
                    Object o = i.next();
                    if (o instanceof ObjectInstance) {
                        ObjectInstance inst = (ObjectInstance) o;
                        ObjectName name = inst.getObjectName();
                        try {
                            log.debug("Unregistering " + name);
                            mbServer.unregisterMBean(name);
                        } catch (Throwable t) {
                            log.error("Error unregistering " + name);
                        }
                    }
                }
            }

            if (bootMLetName != null) {
                try {
                    log.info("Unregistering MLet " + bootMLetName);
                    mbServer.unregisterMBean(bootMLetName);
                } catch (Throwable t) {
                    log.error("Error unregistering MLet ", t);
                }
            }

            if (mbServer != null) {
                try {
                    log.info("Releasing MBeanServer");
                    MBeanServerFactory.releaseMBeanServer(mbServer);
                } catch (Throwable t) {
                    log.error("Error releasing MBeanServer", t);
                }
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
            URL mletURL = new URL("file:modules/core/src/conf/boot.mlet");
            URL deployURL = new URL("file:modules/core/src/conf/boot-service.xml");
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
