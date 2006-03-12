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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.Configuration;


/**
 * @version $Rev$ $Date$
 */
public class RefContext {

    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final ResourceReferenceBuilder resourceReferenceBuilder;
    private final ServiceReferenceBuilder serviceReferenceBuilder;
    private final Kernel kernel;

    private final Map messageDestinations = new HashMap();

    public RefContext(EJBReferenceBuilder ejbReferenceBuilder, ResourceReferenceBuilder resourceReferenceBuilder, ServiceReferenceBuilder serviceReferenceBuilder, Kernel kernel) {
        assert ejbReferenceBuilder != null: "ejbReferenceBuilder is null";
        assert resourceReferenceBuilder != null: "resourceReferenceBuilder is null";
        assert serviceReferenceBuilder != null: "serviceReferenceBuilder is null";

        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
        this.kernel = kernel;
    }

    public static RefContext derivedClientRefContext(RefContext refContext, EJBReferenceBuilder ejbReferenceBuilder, ResourceReferenceBuilder resourceReferenceBuilder, ServiceReferenceBuilder serviceReferenceBuilder) {
        return new RefContext(refContext, ejbReferenceBuilder, resourceReferenceBuilder, serviceReferenceBuilder);
    }

    private RefContext(RefContext refContext, EJBReferenceBuilder ejbReferenceBuilder, ResourceReferenceBuilder resourceReferenceBuilder, ServiceReferenceBuilder serviceReferenceBuilder) {
        assert ejbReferenceBuilder != null: "ejbReferenceBuilder is null";
        assert resourceReferenceBuilder != null: "resourceReferenceBuilder is null";
        assert refContext != null: "ejbRefContext is null";

        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
        this.kernel = refContext.kernel;
    }

    //registration methods

    public void registerMessageDestionations(String moduleName, Map nameMap) throws DeploymentException {
        messageDestinations.put(moduleName, nameMap);
    }


    //lookup methods

    public Reference getEJBRemoteRef(String objectName, boolean isSession, String home, String remote) throws DeploymentException {
        return ejbReferenceBuilder.createEJBRemoteReference(objectName, null, isSession, home, remote);
    }

    public Reference getCORBARemoteRef(Artifact configId, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
        return ejbReferenceBuilder.createCORBAReference(configId, containerNameQuery, nsCorbaloc, objectName, home);
    }

    public Reference getEJBLocalRef(String objectName, boolean isSession, String localHome, String local) throws DeploymentException {
        return ejbReferenceBuilder.createEJBLocalReference(objectName, null, isSession, localHome, local);
    }

    public Object getHandleDelegateReference() throws DeploymentException {
        return ejbReferenceBuilder.createHandleDelegateReference();
    }

    public Reference getEJBRemoteRef(URI module, String ejbLink, boolean isSession, String home, String remote, NamingContext namingContext) throws DeploymentException {
        GBeanData containerData = locateEjbInApplication(namingContext, isSession, ejbLink, module);
        return ejbReferenceBuilder.createEJBRemoteReference(containerData.getName().getCanonicalName(), containerData, isSession, home, remote);
    }

    public Reference getEJBLocalRef(URI module, String ejbLink, boolean isSession, String localHome, String local, NamingContext namingContext) throws DeploymentException {
        GBeanData containerData = locateEjbInApplication(namingContext, isSession, ejbLink, module);
        return ejbReferenceBuilder.createEJBLocalReference(containerData.getName().getCanonicalName(), containerData, isSession, localHome, local);
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

    public String getResourceAdapterContainerId(URI moduleURI, String resourceLink, NamingContext context) throws UnresolvedReferenceException {
        AbstractName moduleName = context.getModuleName();
        AbstractName containerName = locateComponentName(resourceLink, moduleURI, NameFactory.JCA_RESOURCE, NameFactory.JCA_RESOURCE_ADAPTER, moduleName, context, "resource adapter");
        return containerName.toString();
    }

    public String getConnectionFactoryContainerId(URI moduleURI, String resourceLink, String type, NamingContext context) throws UnresolvedReferenceException {
        AbstractName moduleName = context.getModuleName();
        AbstractName containerName = locateComponentName(resourceLink, moduleURI, NameFactory.JCA_RESOURCE, type, moduleName, context, "connection factory");
        return containerName.toString();
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

    public String getAdminObjectContainerId(URI moduleURI, String resourceLink, NamingContext context) throws DeploymentException {
        AbstractName moduleName = context.getModuleName();
        AbstractName containerName = locateComponentName(resourceLink, moduleURI, NameFactory.JCA_RESOURCE, NameFactory.JCA_ADMIN_OBJECT, moduleName, context, "admin object");
        return containerName.toString();
    }

    public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote, NamingContext namingContext) throws DeploymentException {
        return ejbReferenceBuilder.getImplicitEJBRemoteRef(module, refName, isSession, home, remote, namingContext);
    }

    public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local, NamingContext namingContext) throws DeploymentException {
        return ejbReferenceBuilder.getImplicitEJBLocalRef(module, refName, isSession, localHome, local, namingContext);
    }


    //Resource adapter/activationspec support

//    public GBeanData getResourceAdapterGBeanData(ObjectName resourceAdapterModuleName, NamingContext context) throws DeploymentException {
//        GBeanData resourceModuleData = locateComponentData(resourceAdapterModuleName, context);
//        return resourceReferenceBuilder.locateResourceAdapterGBeanData(resourceModuleData);
//    }

    public GBeanData getActivationSpecInfo(AbstractName resourceAdapterModuleName, String messageListenerInterfaceName, NamingContext context) throws DeploymentException {
        GBeanData resourceModuleData = locateComponentData(resourceAdapterModuleName, context);
        return resourceReferenceBuilder.locateActivationSpecInfo(resourceModuleData, messageListenerInterfaceName);
    }

//    //this relies on finding the resource adapter, not the admin object.
//    public GBeanData getAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName, NamingContext context) throws DeploymentException {
//        GBeanData resourceModuleData = locateComponentData(resourceAdapterModuleName, context);
//        return resourceReferenceBuilder.locateAdminObjectInfo(resourceModuleData, adminObjectInterfaceName);
//    }
//
//    public GBeanData getConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName, NamingContext context) throws DeploymentException {
//        GBeanData resourceModuleData = locateComponentData(resourceAdapterModuleName, context);
//        return resourceReferenceBuilder.locateConnectionFactoryInfo(resourceModuleData, connectionFactoryInterfaceName);
//    }

    public String getMEJBName() throws DeploymentException {
        AbstractNameQuery query = new AbstractNameQuery(null, Collections.singletonMap("name", "ejb/mgmt/MEJB"), (String)null);
        AbstractName mejbName = locateUniqueNameInKernel(query, "Management EJB");
        return mejbName.toString();
    }

    public AbstractName locateComponentName(String resourceLink, URI moduleURI, String moduleType, String type, AbstractName j2eeContext, NamingContext context, String queryType) throws UnresolvedReferenceException {
        GBeanData match = locateComponent(resourceLink, moduleURI, moduleType, type, j2eeContext, context, queryType);
        return match.getAbstractName();
    }

    private GBeanData locateComponent(String resourceLink, URI moduleURI, String moduleType, String type, AbstractName j2eeContext, NamingContext context, String queryType) throws UnresolvedReferenceException {
        GBeanData match = locateComponentInApplication(resourceLink, moduleURI, moduleType, type, j2eeContext, context, true);
        if (match == null) {
            //no matches in current context, look in other modules with J2EEApplication=null
            return locateGBeanInKernel(resourceLink, type, j2eeContext, queryType);
        }
        return match;
    }

    private GBeanData locateComponentData(AbstractName name, NamingContext context) throws UnresolvedReferenceException {
        try {
            return context.getGBeanInstance(name);
        } catch (GBeanNotFoundException ignored) {
        }
        try {
            return kernel.getGBeanData(name);
        } catch (GBeanNotFoundException e) {
            throw new UnresolvedReferenceException("GBean name: " + name + " not found in DeploymentContext: " + context.getConfigID() + " or in kernel", false, null);
        }
    }

    private GBeanData locateEjbInApplication(NamingContext namingContext, boolean isSession, String ejbLink, URI module) throws UnresolvedReferenceException {
        GBeanData gbeanData;
        AbstractName moduleName = namingContext.getModuleName();
        if (isSession) {
            gbeanData = locateComponentInApplication(ejbLink, module, NameFactory.EJB_MODULE, NameFactory.STATELESS_SESSION_BEAN, moduleName, namingContext, false);
            if (gbeanData == null) {
                gbeanData = locateComponentInApplication(ejbLink, module, NameFactory.EJB_MODULE, NameFactory.STATEFUL_SESSION_BEAN, moduleName, namingContext, true);
            }
        } else {
            gbeanData = locateComponentInApplication(ejbLink, module, NameFactory.EJB_MODULE, NameFactory.ENTITY_BEAN, moduleName, namingContext, true);
        }
        return gbeanData;
    }

    private GBeanData locateComponentInApplication(String resourceLink, URI moduleURI, String moduleType, String type, AbstractName j2eeContext, NamingContext context, boolean requireMatchInExplicitModule) throws UnresolvedReferenceException {
        GBeanData match = locateComponentInModule(resourceLink, moduleURI, moduleType, type, j2eeContext, context, requireMatchInExplicitModule);
        if (match == null) {
            //if we got this far we resourceLink has no #.  look in "any module" in this application
            match = locateGBeanInContext(null, "*", resourceLink, type, j2eeContext, context, false);
        }
        return match;
    }

    private GBeanData locateComponentInModule(String resourceLink, URI moduleURI, String moduleType, String type, AbstractName j2eeContext, NamingContext context, boolean requireMatchInExplicitModule) throws UnresolvedReferenceException {
        String name = resourceLink.substring(resourceLink.lastIndexOf('#') + 1);

        if (resourceLink.indexOf('#') > -1) {
            //presence of # means they explicitly want only gbeans in specified module in this application.
            String module = moduleURI.resolve(resourceLink).getPath();
            return locateGBeanInContext(moduleType, module, name, type, j2eeContext, context, requireMatchInExplicitModule);
        } else if (moduleURI == null || moduleURI.equals("")) {
            return locateGBeanInContext(moduleType, "*", name, type, j2eeContext, context, false);
        } else {
            return locateGBeanInContext(moduleType, moduleURI.getPath(), name, type, j2eeContext, context, false);
        }
    }

    private GBeanData locateGBeanInContext(String moduleType, String moduleName, String name, String type, AbstractName j2eeContext, NamingContext context, boolean requireMatch) throws UnresolvedReferenceException {
        //TODO make sure this is reasonable
        if (moduleType == null) {
            moduleName = "*";
        }

        AbstractNameQuery query = NameFactory.getComponentNameQuery(moduleName, moduleType, name, type, j2eeContext);

        AbstractName match = locateUniqueNameInContext(context, query);
        if (match == null) {
            if (!requireMatch) {
                return null;
            } else {
                throw new UnresolvedReferenceException("Could not resolve reference: module: " + moduleName + ", component name: " + name, false, query.toString());
            }
        }
        try {
            GBeanData data = context.getGBeanInstance(match);
            return data;
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("BUG! context listed a gbean but could not get its gbeanData: " + match + " gbeans in context:" + context.getGBeanNames());
        }
    }

    private AbstractName locateUniqueNameInContext(NamingContext context, AbstractNameQuery query) throws UnresolvedReferenceException {
        Set matches = context.listGBeans(query);

        // too big
        if (matches.size() > 1) {
            throw new UnresolvedReferenceException("More than one match for query " + matches, true, query.toString());
        }

        // just right
        if (matches.size() == 1) {
            return (AbstractName) matches.iterator().next();
        }

        // never mind
        return null;
    }

    private GBeanData locateGBeanInKernel(String name, String type, AbstractName j2eeContext, String queryType) throws UnresolvedReferenceException {
        AbstractNameQuery query = NameFactory.getComponentNameQuery(name, type, j2eeContext);
        return locateUniqueGBeanData(query, queryType);
    }


    private AbstractName locateUniqueNameInKernel(AbstractNameQuery query, String type) throws UnresolvedReferenceException {
        Set names = kernel.listGBeans(query);
        if (names.size() != 1) {
            throw new UnresolvedReferenceException(type, names.size() > 1, query.toString());
        }
        return (AbstractName) names.iterator().next();
    }

    private GBeanData locateUniqueGBeanData(AbstractNameQuery query, String type) throws UnresolvedReferenceException {
        AbstractName match = locateUniqueNameInKernel(query, type);
        try {
            return kernel.getGBeanData(match);
        } catch (GBeanNotFoundException e) {
            throw new IllegalStateException("BUG! kernel listed a gbean but could not get its gbeanData: " + match);

        }
    }

    public AbstractName locateUniqueName(NamingContext context, AbstractNameQuery query) throws UnresolvedReferenceException {
        AbstractName match = locateUniqueNameInContext(context, query);
        if (match == null) {
            match = locateUniqueNameInKernel(query, "type unknown");
        }
        return match;
    }

}
