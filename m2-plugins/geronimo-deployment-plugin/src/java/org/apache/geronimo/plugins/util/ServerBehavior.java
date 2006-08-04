/**
 *
 * Copyright 2006 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.plugins.util;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.kernel.config.ConfigurationUtil;
import org.apache.geronimo.kernel.config.ConfigurationManager;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.NoSuchAttributeException;
import org.apache.geronimo.system.jmx.KernelDelegate;

/**
 * ???
 * 
 * @version $Rev$ $Date$
 */
public class ServerBehavior {

    private int maxTries = 100;

    private int retryIntervalMilliseconds = 1000;

    private String uri = "jmx:rmi://localhost/jndi/rmi:/JMXConnector";

    private String username = "system";

    private String password = "manager";

    private PrintStream logStream = System.out;

    protected final String lineSep = "===========================================";

    /**
     * @param uri
     *            specify null for default
     * @param maxTries
     *            specify -1 for default. default-value=40
     * @param retryIntervalMilliseconds
     *            specify -1 for default. default-value=1000
     */
    public ServerBehavior(String uri, int maxTries, int retryIntervalMilliseconds) {
        if (uri != null)
            this.uri = uri;
        if (maxTries > -1)
            this.maxTries = maxTries;
        if (retryIntervalMilliseconds > -1)
            this.retryIntervalMilliseconds = retryIntervalMilliseconds;
    }

    public ServerBehavior(String uri) {
        this(uri, -1, -1);
    }

    public ServerBehavior() {
        this(null, -1, -1);
    }

    public boolean isFullyStarted() {
        Kernel kernel = null;

        if (!uri.startsWith("jmx")) {
            logStream.println("Bad JMX URI (" + uri + ")");
            logStream.println(lineSep);
            return false;
        }

        // Get the kernel first
        Map environment = new HashMap();
        String[] credentials = new String[] { username, password };
        environment.put(JMXConnector.CREDENTIALS, credentials);
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            JMXServiceURL address = new JMXServiceURL("service:" + uri);
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            for (int tries = maxTries; true; tries--) {
                try {
                    JMXConnector jmxConnector = JMXConnectorFactory.connect(address, environment);
                    MBeanServerConnection mbServerConnection = jmxConnector.getMBeanServerConnection();
                    kernel = new KernelDelegate(mbServerConnection);
                    break;
                }
                catch (Exception e) {
                    if (tries == 0) {
                        e.printStackTrace(logStream);
                        logStream.println("Could not connect");
                        logStream.println(lineSep);
                        return false;
                    }
                    Thread.sleep(retryIntervalMilliseconds);
                }
            }
        }
        catch (Exception e1) {
            e1.printStackTrace(logStream);
            logStream.println(lineSep);
            return false;
        }
        finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }

        // Now check to see if all configurations have started
        ConfigurationManager mgr = ConfigurationUtil.getConfigurationManager(kernel);
        List configLists = mgr.listConfigurations();
        ObjectName on = null;
        if (!configLists.isEmpty())
            on = (ObjectName) configLists.toArray()[0];

        for (int tries = maxTries; tries > 0; tries--) {
            try {
                Thread.sleep(retryIntervalMilliseconds);
                Boolean b = (Boolean) kernel.getAttribute(on, "kernelFullyStarted");
                //System.out.println("attempt.. " + (maxTries - tries));
                if (b.booleanValue())
                    return true;
            }
            catch (InternalKernelException e) {
                //hasn't been loaded yet, keep trying
            }
            catch (GBeanNotFoundException e) {
                //hasn't been loaded yet, keep trying
            }
            catch (InterruptedException e) {
                e.printStackTrace(logStream);
                logStream.println(lineSep);
                return false;
            }
            catch (NoSuchAttributeException e) {
                e.printStackTrace(logStream);
                logStream.println(lineSep);
                return false;
            }
            catch (Exception e) {
                e.printStackTrace(logStream);
                logStream.println(lineSep);
                return false;
            }
        }
        return false;
    }  
    
    public void setLogStream(PrintStream logStream) {
        if (logStream != null)
            this.logStream = logStream;
    }
    
    public void destroy() {
        logStream.close();
    }
}
