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

import java.net.URL;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.loading.MLet;

import org.apache.log4j.Logger;

/**
 *
 *
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:42:53 $
 */
public class Main implements Runnable {
    private static final Logger log = Logger.getLogger("Geronimo");

    private String domainName;
    private URL bootURL;

    private MBeanServer mbServer;
    private ObjectName serverName;
    private ObjectName bootMLetName;
    private Set bootedMBeans;

    public Main(String domainName, URL url) {
        this.domainName = domainName;
        bootURL = url;
    }

    /**
     * Main entry point
     */
    public void run() {
        ShutdownThread hook = new ShutdownThread("Shutdown-Thread", Thread.currentThread());
        try {
            Runtime.getRuntime().addShutdownHook(hook);
            try {
                init();
            } catch (Throwable e) {
                log.error("Error starting Server", e);
                return;
            }

            // and now go to sleep until exit
            try {
                synchronized (this) {
                    wait();
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
            shutdown();
        }
    }

    private void init() throws Throwable {
        long start = System.currentTimeMillis();

        log.info("Starting MBeanServer");
        mbServer = MBeanServerFactory.createMBeanServer(domainName);

        bootMLet(bootURL);
        serverName = findServer();

        // start her up
        log.info("Initializing Server " + serverName);
        mbServer.invoke(serverName, "init", null, null);

        log.info("Starting Server " + serverName);
        mbServer.invoke(serverName, "start", null, null);

        long end = System.currentTimeMillis();
        log.info("Started Server in " + (end - start) + "ms.");
    }

    private void shutdown() {
        if (serverName != null) {
            try {
                log.info("Stopping Server");
                mbServer.invoke(serverName, "stop", null, null);
                mbServer.invoke(serverName, "destroy", null, null);
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

    private void bootMLet(URL url) throws Throwable {
        log.info("Booting from URL " + url);

        MLet bootMLet = new MLet();
        bootMLetName = mbServer.registerMBean(bootMLet, new ObjectName("geronimo.boot:type=BootMLet,url=\"" + url + "\"")).getObjectName();
        bootedMBeans = bootMLet.getMBeansFromURL(url);

        // check they all loaded OK
        for (Iterator i = bootedMBeans.iterator(); i.hasNext();) {
            Object o = i.next();
            if (o instanceof Throwable) {
                throw (Throwable) o;
            }
        }
    }

    private ObjectName findServer() throws Exception {
        // find which one (and only one) is our Server
        Set serverMBeans = mbServer.queryNames(new ObjectName("geronimo.server:type=Server,*"), null);
        if (serverMBeans.size() == 0) {
            throw new Exception("No Server found (ObjectName must match \"" + serverName + "\")");
        } else if (serverMBeans.size() > 1) {
            throw new Exception("Multiple Servers found (ObjectName matching \"" + serverName + "\")");
        }
        return (ObjectName) serverMBeans.iterator().next();
    }

    /**
     * Command line entry point.
     * Starts a new ThreadGroup so that all owned threads can be identified.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        URL url;
        try {
            url = new URL("file:server/default/config/boot.mlet");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return;
        }
        Main main = new Main(args[0], url); // @todo you should not need to explicitly name the MBean Server

        ThreadGroup group = new ThreadGroup("Geronimo");
        Thread mainThread = new Thread(group, main, "Main-Thread");
        mainThread.start();
    }

    private static class ShutdownThread extends Thread {
        private final Thread mainThread;

        private ShutdownThread(String name, Thread mainThread) {
            super(name);
            this.mainThread = mainThread;
        }

        public void run() {
            mainThread.interrupt();
            try {
                mainThread.join();
            } catch (InterruptedException e) {
            }
        }
    }
}
