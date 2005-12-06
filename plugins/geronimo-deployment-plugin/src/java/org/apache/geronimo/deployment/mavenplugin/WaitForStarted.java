/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.deployment.mavenplugin;

import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.InternalKernelException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.KernelDelegate;
import org.apache.geronimo.kernel.management.State;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WaitForStarted extends AbstractModuleCommand {

    private int maxTries = 40;
    private int retryIntervalMilliseconds = 1000;

    private MBeanServerConnection mbServerConnection;
    private Kernel kernel;
    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setMaxTries(int maxTries) {
        this.maxTries = maxTries;
    }

    public void setRetryIntervalMilliseconds(int retryIntervalMilliseconds) {
        this.retryIntervalMilliseconds = retryIntervalMilliseconds;
    }

    public void execute() throws Exception {
        String uri = getUri();
        if (!uri.startsWith("jmx")) {
            throw new Exception("Bad JMX URI ("+uri+")");
        }

        Map environment = new HashMap();
        String[] credentials = new String[]{getUsername(), getPassword()};
        environment.put(JMXConnector.CREDENTIALS, credentials);

        JMXServiceURL address = new JMXServiceURL("service:" + uri);
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
            for (int tries = maxTries; true; tries--) {
                try {
                    JMXConnector jmxConnector = JMXConnectorFactory.connect(address, environment);
                    mbServerConnection = jmxConnector.getMBeanServerConnection();
                    kernel = new KernelDelegate(mbServerConnection);
                    break;
                } catch (Exception e) {
                    if (tries == 0) {
                        throw new Exception("Could not connect");
                    }
                    Thread.sleep(retryIntervalMilliseconds);
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldcl);
        }
        ObjectName configName = Configuration.getConfigurationObjectName(new URI(getId()));
        for (int tries = maxTries; tries > 0; tries--) {
            try {
                int state = kernel.getGBeanState(configName);
                if (state == State.RUNNING_INDEX) {
                    return;
                }
            } catch (InternalKernelException e) {
                //hasn't been loaded yet, keep trying
            } catch (GBeanNotFoundException e) {
                //hasn't been loaded yet, keep trying
            }
            Thread.sleep(retryIntervalMilliseconds);
        }
        throw new Exception("Configuration is not yet started");
    }

}
