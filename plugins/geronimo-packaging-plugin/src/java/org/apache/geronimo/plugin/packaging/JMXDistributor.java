/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.plugin.packaging;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.apache.geronimo.common.DeploymentException;

/**
 * A Distributor that will distribute packages to a server using JMX remoting.
 * Currently this is limited to a server on the same machine (because the
 * location of the artifact is passed using a file: URL).
 *
 * @version $Rev$ $Date$
 */
public class JMXDistributor extends AbstractDistributor {
    public void execute() throws Exception {
        if (!getArtifact().canRead()) {
            throw new DeploymentException("Unable to read artifact " + getArtifact());
        }

        Map environment = new HashMap();
        environment.put(JMXConnector.CREDENTIALS, new String[]{getUser(), getPassword()});

        JMXServiceURL serviceURL = new JMXServiceURL(getUrl());
        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceURL, environment);
        try {
            MBeanServerConnection mbServer = jmxConnector.getMBeanServerConnection();

            ObjectName configStore = locateConfigStore(mbServer, storeName);
            mbServer.invoke(configStore, "install", new Object[]{getArtifact().toURL()}, new String[]{URL.class.getName()});
            System.out.println("Distributed " + getArtifact() + " to " + getUrl());
        } finally {
            jmxConnector.close();
        }
    }

    private ObjectName locateConfigStore(MBeanServerConnection mbServer, ObjectName storeName) throws Exception {
        Set set = mbServer.queryNames(storeName, null);
        Iterator i = set.iterator();
        if (!i.hasNext()) {
            throw new DeploymentException("No ConfigurationStore found matching " + storeName);
        }
        ObjectName configStore = (ObjectName) i.next();
        if (i.hasNext()) {
            throw new DeploymentException("Multiple ConfigurationStores found matching " + storeName);
        }
        return configStore;
    }
}
