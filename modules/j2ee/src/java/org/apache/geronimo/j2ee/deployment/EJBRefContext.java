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

import javax.naming.Reference;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class EJBRefContext {
    private final EJBReferenceBuilder ejbReferenceBuilder;
    private final Map ejbRemoteIndex;
    private final Map ejbLocalIndex;
    private final Map ejbInterfaceIndex;

    public EJBRefContext(EJBReferenceBuilder ejbReferenceBuilder) {
        assert ejbReferenceBuilder != null: "ejbReferenceBuilder is null";

        ejbRemoteIndex = new HashMap();
        ejbLocalIndex = new HashMap();
        ejbInterfaceIndex = new HashMap();
        this.ejbReferenceBuilder = ejbReferenceBuilder;
    }

    public EJBRefContext(EJBRefContext ejbRefContext, EJBReferenceBuilder ejbReferenceBuilder) {
        assert ejbReferenceBuilder != null: "ejbReferenceBuilder is null";
        assert ejbRefContext != null: "ejbRefContext is null";

        this.ejbReferenceBuilder = ejbReferenceBuilder;
        this.ejbRemoteIndex = ejbRefContext.ejbRemoteIndex;
        this.ejbLocalIndex = ejbRefContext.ejbLocalIndex;
        this.ejbInterfaceIndex = ejbRefContext.ejbInterfaceIndex;
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
            URI moduelURI = new URI(null, null, modulePath.getPath(), null);
            interfacesReferences.put(moduelURI, containerId);
        } catch (URISyntaxException e) {
            throw new DeploymentException(e);
        }
    }

    public Reference getEJBRemoteRef(String objectName, boolean isSession, String home, String remote) throws DeploymentException {
        if (ejbReferenceBuilder == null) {
            throw new DeploymentException("No ejb reference builder");
        }
        return ejbReferenceBuilder.createEJBRemoteReference(objectName, isSession, home, remote);
    }

    public Reference getEJBLocalRef(String objectName, boolean isSession, String localHome, String local) throws DeploymentException {
        if (ejbReferenceBuilder == null) {
            throw new DeploymentException("No ejb reference builder");
        }
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
            throw new UnresolvedEJBRefException(refName, ejbRefInfo, false);
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
        throw new UnresolvedEJBRefException(refName, ejbRefInfo, true);
    }

}
