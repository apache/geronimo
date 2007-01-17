/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.openejb.deployment.ejbref;

import java.net.URI;
import java.net.URISyntaxException;
import javax.naming.Reference;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.ClientEjbReference;

/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class ClientEjbRefBuilder extends RemoteEjbRefBuilder {
    private final URI uri;

    public ClientEjbRefBuilder(Environment defaultEnvironment, String[] eeNamespaces, String host, int port) throws URISyntaxException {
        super(defaultEnvironment, eeNamespaces);
        uri = new URI("ejb", null, host, port, null, null, null);
    }

    protected Reference createEjbRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, AbstractNameQuery query, boolean isSession, String homeInterface, String businessInterface, boolean remote) throws DeploymentException {
        AbstractNameQuery match = getEjbRefQuery(refName, configuration, name, requiredModule, optionalModule, query, isSession, homeInterface, businessInterface, remote);

        GBeanData data = null;
        try {
            data = configuration.findGBeanData(match);
        } catch (GBeanNotFoundException ignored) {
            throw new DeploymentException("Ejb not found for ejb-ref " + refName);
        }

        String deploymentId = (String) data.getAttribute("deploymentId");
        if (deploymentId == null) {
            throw new DeploymentException(("EjbDeployment GBeanData does not contain a \"deploymentId\" attribute"));
        }
        return new ClientEjbReference(uri.toString(), deploymentId);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ClientEjbRefBuilder.class, RemoteEjbRefBuilder.GBEAN_INFO, NameFactory.MODULE_BUILDER);

        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", int.class, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces", "host", "port"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
