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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.AmbiguousEJBRefException;
import org.apache.geronimo.common.UnknownEJBRefException;
import org.apache.geronimo.common.UnresolvedEJBRefException;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.deployment.DeploymentContext;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class RefContext {

    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final ResourceReferenceBuilder resourceReferenceBuilder;
    private final ServiceReferenceBuilder serviceReferenceBuilder;

    private final Map ejbRemoteIndex;
    private final Map ejbLocalIndex;
    private final Map ejbInterfaceIndex;

    private final Map resourceAdapterIndex;
    private final Map connectionFactoryIndex;
    private final Map adminObjectIndex;

    private final Map resourceModuleDataMap;


    public RefContext(EJBReferenceBuilder ejbReferenceBuilder, ResourceReferenceBuilder resourceReferenceBuilder, ServiceReferenceBuilder serviceReferenceBuilder) {
        assert ejbReferenceBuilder != null: "ejbReferenceBuilder is null";
        assert resourceReferenceBuilder != null: "resourceReferenceBuilder is null";
        assert serviceReferenceBuilder != null: "serviceReferenceBuilder is null";

        ejbRemoteIndex = new HashMap();
        ejbLocalIndex = new HashMap();
        ejbInterfaceIndex = new HashMap();
        resourceAdapterIndex = new HashMap();
        connectionFactoryIndex = new HashMap();
        adminObjectIndex = new HashMap();
        resourceModuleDataMap = new HashMap();
        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.resourceReferenceBuilder = resourceReferenceBuilder;
        this.serviceReferenceBuilder = serviceReferenceBuilder;
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
        this.ejbRemoteIndex = refContext.ejbRemoteIndex;
        this.ejbLocalIndex = new HashMap();//no local ejb refs
        this.ejbInterfaceIndex = refContext.ejbInterfaceIndex;
        resourceAdapterIndex = new HashMap();
        this.connectionFactoryIndex = new HashMap();
        this.adminObjectIndex = new HashMap();
        this.resourceModuleDataMap = new HashMap();
    }

    public EJBReferenceBuilder getEjbReferenceBuilder() {
        return ejbReferenceBuilder;
    }

    public Map getEJBRemoteIndex() {
        return ejbRemoteIndex;
    }

    public Map getEJBLocalIndex() {
        return ejbLocalIndex;
    }

    public Map getConnectionFactoryIndex() {
        return connectionFactoryIndex;
    }

    public Map getAdminObjectIndex() {
        return adminObjectIndex;
    }

    public void addEJBRemoteId(URI modulePath, String name, String containerId, boolean isSession, String home, String remote) throws DeploymentException {
        Map references = (Map) ejbRemoteIndex.get(name);
        if (references == null || references.isEmpty()) {
            references = new HashMap();
            ejbRemoteIndex.put(name, references);
        }

        EJBRefInfo ejbRefInfo = new EJBRefInfo(false, isSession, home, remote);
        Map interfacesReferences = (Map) ejbInterfaceIndex.get(ejbRefInfo);
        if (interfacesReferences == null || interfacesReferences.isEmpty()) {
            interfacesReferences = new HashMap();
            ejbInterfaceIndex.put(ejbRefInfo, interfacesReferences);
        }

        addEJBId(modulePath, name, containerId, references, interfacesReferences);
    }

    public void addEJBLocalId(URI modulePath, String name, String containerId, boolean isSession, String localHome, String local) throws DeploymentException {
        Map references = (Map) ejbLocalIndex.get(name);
        if (references == null || references.isEmpty()) {
            references = new HashMap();
            ejbLocalIndex.put(name, references);
        }

        EJBRefInfo ejbRefInfo = new EJBRefInfo(true, isSession, localHome, local);
        Map interfacesReferences = (Map) ejbInterfaceIndex.get(ejbRefInfo);
        if (interfacesReferences == null || interfacesReferences.isEmpty()) {
            interfacesReferences = new HashMap();
            ejbInterfaceIndex.put(ejbRefInfo, interfacesReferences);
        }

        addEJBId(modulePath, name, containerId, references, interfacesReferences);
    }

    private void addEJBId(URI modulePath, String name, String containerId, Map references, Map interfacesReferences) throws DeploymentException {
        try {
            URI ejbURI = new URI(null, null, modulePath.getPath(), name);
            references.put(ejbURI, containerId);
            URI moduleURI = new URI(null, null, modulePath.getPath(), null);
            interfacesReferences.put(moduleURI, containerId);
        } catch (URISyntaxException e) {
            throw new DeploymentException(e);
        }
    }

    public void addResourceAdapterId(URI modulePath, String name, String containerId) throws DeploymentException {
        Map references = (Map) resourceAdapterIndex.get(name);
        if (references == null || references.isEmpty()) {
            references = new HashMap();
            resourceAdapterIndex.put(name, references);
        }

        try {
            URI cfURI = new URI(null, null, modulePath.getPath(), name);
            references.put(cfURI, containerId);
        } catch (URISyntaxException e) {
            throw new DeploymentException(e);
        }
    }

    public void addConnectionFactoryId(URI modulePath, String name, String containerId) throws DeploymentException {
        Map references = (Map) connectionFactoryIndex.get(name);
        if (references == null || references.isEmpty()) {
            references = new HashMap();
            connectionFactoryIndex.put(name, references);
        }

        try {
            URI cfURI = new URI(null, null, modulePath.getPath(), name);
            references.put(cfURI, containerId);
        } catch (URISyntaxException e) {
            throw new DeploymentException(e);
        }
    }


    public void addAdminObjectId(URI modulePath, String name, String containerId) throws DeploymentException {
        Map references = (Map) adminObjectIndex.get(name);
        if (references == null || references.isEmpty()) {
            references = new HashMap();
            adminObjectIndex.put(name, references);
        }

        try {
            URI cfURI = new URI(null, null, modulePath.getPath(), name);
            references.put(cfURI, containerId);
        } catch (URISyntaxException e) {
            throw new DeploymentException(e);
        }
    }



    //lookup methods

    public Reference getEJBRemoteRef(String objectName, boolean isSession, String home, String remote) throws DeploymentException {
        return ejbReferenceBuilder.createEJBRemoteReference(objectName, isSession, home, remote);
    }

    public Reference getEJBLocalRef(String objectName, boolean isSession, String localHome, String local) throws DeploymentException {
        return ejbReferenceBuilder.createEJBLocalReference(objectName, isSession, localHome, local);
    }

    public Reference getEJBRemoteRef(URI module, String ejbLink, boolean isSession, String home, String remote) throws DeploymentException {
        String name = ejbLink.substring(ejbLink.lastIndexOf('#') + 1);
        String containerId = getContainerId(module, ejbLink, (Map) ejbRemoteIndex.get(name));
        return getEJBRemoteRef(containerId, isSession, home, remote);
    }

    public Reference getEJBLocalRef(URI module, String ejbLink, boolean isSession, String localHome, String local) throws DeploymentException {
        String name = ejbLink.substring(ejbLink.lastIndexOf('#') + 1);
        String containerId = getContainerId(module, ejbLink, (Map) ejbLocalIndex.get(name));
        return getEJBLocalRef(containerId, isSession, localHome, local);
    }

    public Reference getConnectionFactoryRef(String containerId, Class iface) throws DeploymentException {
        return resourceReferenceBuilder.createResourceRef(containerId, iface);
    }

    public String getResourceAdapterContainerId(URI module, String resourceLink, J2eeContext j2eeContext) throws DeploymentException, UnknownEJBRefException {
        String name = resourceLink.substring(resourceLink.lastIndexOf('#') + 1);
        try {
            return getContainerId(module, resourceLink, (Map) resourceAdapterIndex.get(name));
        } catch (UnknownEJBRefException e) {
            ObjectName query = null;
            try {
                query = NameFactory.getComponentRestrictedQueryName(null, null, name, NameFactory.JCA_RESOURCE_ADAPTER, j2eeContext);
            } catch (MalformedObjectNameException e1) {
                throw new DeploymentException("Could not construct resource adapter object name query", e);
            }
            ObjectName containerName = resourceReferenceBuilder.locateResourceName(query);
            return containerName.getCanonicalName();
        }
    }

    public String getConnectionFactoryContainerId(URI module, String resourceLink, String type, J2eeContext j2eeContext, DeploymentContext context) throws DeploymentException, UnknownEJBRefException {
        String name = resourceLink.substring(resourceLink.lastIndexOf('#') + 1);
        try {
            return getContainerId(module, resourceLink, (Map) connectionFactoryIndex.get(name));
        } catch (UnknownEJBRefException e) {

            ObjectName query = null;
            try {
                query = NameFactory.getComponentNameQuery(null, null, null, name, type, j2eeContext);
            } catch (MalformedObjectNameException e1) {
                throw new DeploymentException("Could not construct connection factory object name query", e);
            }
            Set matches = context.listGBeans(query);
            if (matches.size() > 1) {
                throw new DeploymentException("More than one match for query " + matches);
            }
            if (matches.size() == 1) {
                return ((ObjectName)matches.iterator().next()).getCanonicalName();
            }
            try {
                query = NameFactory.getComponentRestrictedQueryName(null, null, name, type, j2eeContext);
            } catch (MalformedObjectNameException e1) {
                throw new DeploymentException("Could not construct connection factory object name query", e);
            }
            ObjectName containerName = resourceReferenceBuilder.locateResourceName(query);
            return containerName.getCanonicalName();
        }
    }

    public Reference getAdminObjectRef(String containerId, Class iface) throws DeploymentException {
        return resourceReferenceBuilder.createAdminObjectRef(containerId, iface);
    }

    public String getAdminObjectContainerId(URI module, String resourceLink, J2eeContext j2eeContext) throws DeploymentException, UnknownEJBRefException {
        String name = resourceLink.substring(resourceLink.lastIndexOf('#') + 1);
        try {
            return getContainerId(module, resourceLink, (Map) adminObjectIndex.get(name));
        } catch (UnknownEJBRefException e) {
            ObjectName query = null;
            try {
                query = NameFactory.getComponentRestrictedQueryName(null, null, name, NameFactory.JCA_ADMIN_OBJECT, j2eeContext);
            } catch (MalformedObjectNameException e1) {
                throw new DeploymentException("Could not construct admin object object name query", e);
            }
            ObjectName containerName = resourceReferenceBuilder.locateResourceName(query);
            return containerName.getCanonicalName();
        }
    }

    public Object getServiceReference(Class serviceInterface, URI wsdlURI, URI jaxrpcMappingURI, QName serviceQName, Map portComponentRefMap, List handlers, DeploymentContext deploymentContext, Module module, ClassLoader classLoader) throws DeploymentException {
        return serviceReferenceBuilder.createService(serviceInterface, wsdlURI, jaxrpcMappingURI, serviceQName, portComponentRefMap, handlers, deploymentContext, module, classLoader);
    }

    private String getContainerId(URI module, String ejbLink, Map references) throws AmbiguousEJBRefException, UnknownEJBRefException {
        if (references == null || references.isEmpty()) {
            throw new UnknownEJBRefException(ejbLink);
        }

        // is this an absolute reference  ../relative/path/Module#EJBName
        if (ejbLink.indexOf('#') >= 0) {
            URI ejbURI = module.resolve(ejbLink).normalize();
            String ejbRef = (String) references.get(ejbURI);
            if (ejbRef == null) {
                throw new UnknownEJBRefException(ejbLink);
            }
            return ejbRef;
        }

        //
        // relative reference
        //

        // if there is only one ejb with that name, use it
        if (references.size() == 1) {
            String ejbRef = (String) references.values().iterator().next();
            if (ejbRef == null) {
                throw new UnknownEJBRefException(ejbLink);
            }
            return ejbRef;
        }

        // We got more then one ejb with that name.  Try to find an ejb in the current module with that name
        String ejbRef = (String) references.get(module.resolve("#" + ejbLink));
        if (ejbRef != null) {
            return ejbRef;
        }

        // there is more then one ejb with the specifiec name
        throw new AmbiguousEJBRefException(ejbLink);
    }

    public Reference getImplicitEJBRemoteRef(URI module, String refName, boolean isSession, String home, String remote) throws DeploymentException {
        EJBRefInfo ejbRefInfo = new EJBRefInfo(false, isSession, home, remote);
        String containerId = getImplicitContainerId(module, refName, ejbRefInfo);
        return getEJBRemoteRef(containerId, isSession, home, remote);
    }

    public Reference getImplicitEJBLocalRef(URI module, String refName, boolean isSession, String localHome, String local) throws DeploymentException {
        EJBRefInfo ejbRefInfo = new EJBRefInfo(true, isSession, localHome, local);
        String containerId = getImplicitContainerId(module, refName, ejbRefInfo);
        return getEJBLocalRef(containerId, isSession, localHome, local);
    }

    private String getImplicitContainerId(URI module, String refName, EJBRefInfo ejbRefInfo) throws DeploymentException {
        Map references = (Map) ejbInterfaceIndex.get(ejbRefInfo);

        // if we didn't find any ejbs that implement that interface... give up
        if (references == null || references.isEmpty()) {
            throw new UnresolvedEJBRefException(refName, ejbRefInfo.isLocal(), ejbRefInfo.isSession(), ejbRefInfo.getHomeIntf(), ejbRefInfo.getBeanIntf(), false);
        }

        // if there is only one matching ejb, use it
        if (references.size() == 1) {
            return (String) references.values().iterator().next();
        }

        // We got more then one matching ejb.  Try to find an ejb in the current module
        String ejbRef = (String) references.get(module);
        if (ejbRef != null) {
            return ejbRef;
        }

        // there is more then one ejb that implements that interface... give up
        throw new UnresolvedEJBRefException(refName, ejbRefInfo.isLocal(), ejbRefInfo.isSession(), ejbRefInfo.getHomeIntf(), ejbRefInfo.getBeanIntf(), true);
    }

    //Resource adapter/activationspec support

    public void addResourceAdapterModuleInfo(ObjectName resourceModuleName, GBeanData resourceModuleData) throws DeploymentException {
        Object old = resourceModuleDataMap.put(resourceModuleName, resourceModuleData);
        if (old != null) {
            throw new DeploymentException("Duplicate resource adapter module name: " + resourceModuleName);
        }
    }
    
    public GBeanData getResourceAdapterGBeanData(ObjectName resourceAdapterModuleName) throws DeploymentException {
        GBeanData resourceModuleData = (GBeanData) resourceModuleDataMap.get(resourceAdapterModuleName);
        if (resourceModuleData != null) {
            return (GBeanData) resourceModuleData.getAttribute("resourceAdapterGBeanData");
        }
        return resourceReferenceBuilder.locateResourceAdapterGBeanData(resourceAdapterModuleName);
    }

    public GBeanData getActivationSpecInfo(ObjectName resourceAdapterModuleName, String messageListenerInterfaceName) throws DeploymentException {
        GBeanData resourceModuleData = (GBeanData) resourceModuleDataMap.get(resourceAdapterModuleName);
        if (resourceModuleData != null) {
            Map activationSpecInfoMap = (Map) resourceModuleData.getAttribute("activationSpecInfoMap");
            return (GBeanData) activationSpecInfoMap.get(messageListenerInterfaceName);
        }
        return resourceReferenceBuilder.locateActivationSpecInfo(resourceAdapterModuleName, messageListenerInterfaceName);
    }

    public GBeanData getAdminObjectInfo(ObjectName resourceAdapterModuleName, String adminObjectInterfaceName) throws DeploymentException {
        GBeanData resourceModuleData = (GBeanData) resourceModuleDataMap.get(resourceAdapterModuleName);
        if (resourceModuleData != null) {
            Map adminObjectInfoMap = (Map) resourceModuleData.getAttribute("adminObjectInfoMap");
            return (GBeanData) adminObjectInfoMap.get(adminObjectInterfaceName);
        }
        return resourceReferenceBuilder.locateAdminObjectInfo(resourceAdapterModuleName, adminObjectInterfaceName);
    }

    public GBeanData getConnectionFactoryInfo(ObjectName resourceAdapterModuleName, String connectionFactoryInterfaceName) throws DeploymentException {
        GBeanData resourceModuleData = (GBeanData) resourceModuleDataMap.get(resourceAdapterModuleName);
        if (resourceModuleData != null) {
            Map managedConnectionFactoryInfoMap = (Map) resourceModuleData.getAttribute("managedConnectionFactoryInfoMap");
            return (GBeanData) managedConnectionFactoryInfoMap.get(connectionFactoryInterfaceName);
        }
        return resourceReferenceBuilder.locateConnectionFactoryInfo(resourceAdapterModuleName, connectionFactoryInterfaceName);
    }

    public GBeanData getResourceAdapterModuleData(ObjectName resourceAdapterModuleName) {
        return (GBeanData) resourceModuleDataMap.get(resourceAdapterModuleName);
    }
}
