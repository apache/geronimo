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
package org.apache.geronimo.j2ee.deployment;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;

import javax.naming.Reference;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @version $Rev:386276 $ $Date$
 */
public class RefContext {
    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final ResourceReferenceBuilder resourceReferenceBuilder;
    private final ServiceReferenceBuilder serviceReferenceBuilder;

    private final Map messageDestinations = new HashMap();

    public RefContext(EJBReferenceBuilder ejbReferenceBuilder, ResourceReferenceBuilder resourceReferenceBuilder, ServiceReferenceBuilder serviceReferenceBuilder) {
        assert ejbReferenceBuilder != null: "ejbReferenceBuilder is null";
        assert resourceReferenceBuilder != null: "resourceReferenceBuilder is null";
        assert serviceReferenceBuilder != null: "serviceReferenceBuilder is null";

        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
    }

    //registration methods
    public void registerMessageDestionations(String moduleName, Map nameMap) throws DeploymentException {
        messageDestinations.put(moduleName, nameMap);
    }

    //lookup methods
    public Reference getCORBARemoteRef(Configuration configuration, AbstractNameQuery cssNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
        return ejbReferenceBuilder.createCORBAReference(configuration, cssNameQuery, nsCorbaloc, objectName, home);
    }

    public Object getHandleDelegateReference() throws DeploymentException {
        return ejbReferenceBuilder.createHandleDelegateReference();
    }
    public Reference getEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) throws DeploymentException {
        return ejbReferenceBuilder.createEJBRemoteRef(refName, configuration, name, requiredModule,  optionalModule, targetConfigId, query, isSession, home, remote);
    }

    public Reference getEJBLocalRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local) throws DeploymentException {
        return ejbReferenceBuilder.createEJBLocalRef(refName, configuration, name, requiredModule,  optionalModule, targetConfigId, query, isSession, localHome, local);
    }

    public Reference getConnectionFactoryRef(AbstractNameQuery containerId, Class iface, Configuration configuration) throws DeploymentException {
        return resourceReferenceBuilder.createResourceRef(containerId, iface, configuration);
    }

    public Reference getAdminObjectRef(AbstractNameQuery containerId, Class iface, Configuration configuration) throws DeploymentException {
        return resourceReferenceBuilder.createAdminObjectRef(containerId, iface, configuration);
    }

    public Object getServiceReference(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlerInfos, Object serviceRefType, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
        return serviceReferenceBuilder.createService(serviceInterface, wsdlURI, jaxrpcMappingURI, serviceQName, portComponentRefMap, handlerInfos, serviceRefType, deploymentContext, module, classLoader);
    }

    public Object getMessageDestination(String messageDestinationLink) throws DeploymentException {
        Object destination = null;
        int pos = messageDestinationLink.indexOf('#');
        if (pos > -1) {
            String targetModule = messageDestinationLink.substring(0, pos);
            Map destinations = (Map) messageDestinations.get(targetModule);
            // Hmmm...if we don't find the module then something is wrong in the deployment.
            if (destinations == null) {
                StringBuffer sb = new StringBuffer();
                for (Iterator mapIterator = messageDestinations.keySet().iterator(); mapIterator.hasNext();) {
                    sb.append(mapIterator.next()).append("\n");
                }
                throw new DeploymentException("Unknown module " + targetModule + " when processing message destination " + messageDestinationLink +
                        "\nKnown modules in deployable unit are:\n" + sb.toString());
            }
            messageDestinationLink = messageDestinationLink.substring(pos + 1);
            destination = destinations.get(messageDestinationLink);
        } else {
            for (Iterator iterator = messageDestinations.values().iterator(); iterator.hasNext();) {
                Map destinations = (Map) iterator.next();
                Object destinationTest = destinations.get(messageDestinationLink);
                if (destinationTest != null) {
                    if (destination != null) {
                        throw new DeploymentException("Duplicate message destination " + messageDestinationLink + " accessed from a message-destination-link without a module");
                    }
                    destination = destinationTest;
                }
            }
        }
        return destination;
    }


    public GBeanData getActivationSpecInfo(AbstractNameQuery resourceAdapterInstanceName, String messageListenerInterfaceName, Configuration configuration) throws DeploymentException {
        return resourceReferenceBuilder.locateActivationSpecInfo(resourceAdapterInstanceName, messageListenerInterfaceName, configuration);
    }

    public AbstractName getMEJBName(Configuration configuration) throws DeploymentException {
        AbstractNameQuery query = new AbstractNameQuery(null, Collections.singletonMap("name", "ejb/mgmt/MEJB"), (String)null);
        try {
            return configuration.findGBean(query);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate a MEJB in the configuration ancestors");
        }
    }
}
