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

package org.apache.geronimo.connector.deployment;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.UnresolvedReferenceException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.reference.ResourceReference;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerResourceRefType;
import org.apache.geronimo.xbeans.j2ee.ConnectorDocument;
import org.apache.geronimo.xbeans.j2ee.ResourceRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev:$ $Date:$
 */
public class ResourceRefBuilder extends AbstractNamingBuilder {
    private static final String J2EE_NAMESPACE = ConnectorDocument.type.getDocumentElementName().getNamespaceURI();
    private static final QName RESOURCE_REF_QNAME = new QName(J2EE_NAMESPACE, "resource-ref");
    private static final QNameSet RESOURCE_REF_QNAME_SET = QNameSet.singleton(RESOURCE_REF_QNAME);
    private static final QName GER_RESOURCE_REF_QNAME = GerResourceRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_RESOURCE_REF_QNAME_SET = QNameSet.singleton(GER_RESOURCE_REF_QNAME);

    private static final String JAXR_CONNECTION_FACTORY_CLASS = "javax.xml.registry.ConnectionFactory";
    private static final String JAVAX_MAIL_SESSION_CLASS = "javax.mail.Session";


    public void buildEnvironment(XmlObject specDD, XmlObject plan, Environment environment) {
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] resourceRefsUntyped = specDD.selectChildren(RESOURCE_REF_QNAME_SET);
        XmlObject[] gerResourceRefsUntyped = plan == null? NO_REFS: plan.selectChildren(GER_RESOURCE_REF_QNAME_SET);
        Map refMap = mapResourceRefs(gerResourceRefsUntyped);
        ClassLoader cl = localConfiguration.getConfigurationClassLoader();

        for (int i = 0; i < resourceRefsUntyped.length; i++) {
            ResourceRefType resourceRef = (ResourceRefType) resourceRefsUntyped[i];
            String name = resourceRef.getResRefName().getStringValue().trim();
            String type = resourceRef.getResType().getStringValue().trim();
            GerResourceRefType gerResourceRef = (GerResourceRefType) refMap.get(name);
            Class iface;
            try {
                iface = cl.loadClass(type);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("could not load class " + type, e);
            }
            if (iface == URL.class) {
                if (gerResourceRef == null || !gerResourceRef.isSetUrl()) {
                    throw new DeploymentException("No url supplied to resolve: " + name);
                }
                try {
                    //TODO expose jsr-77 objects for these guys
                    componentContext.put(ENV + name, new URL(gerResourceRef.getUrl()));
                } catch (MalformedURLException e) {
                    throw new DeploymentException("Could not convert " + gerResourceRef.getUrl() + " to URL", e);
                }
            } else {
                //determine jsr-77 type from interface
                String j2eeType;


                if (JAVAX_MAIL_SESSION_CLASS.equals(type)) {
                    j2eeType = NameFactory.JAVA_MAIL_RESOURCE;
                } else if (JAXR_CONNECTION_FACTORY_CLASS.equals(type)) {
                    j2eeType = NameFactory.JAXR_CONNECTION_FACTORY;
                } else {
                    j2eeType = NameFactory.JCA_MANAGED_CONNECTION_FACTORY;
                }
                try {
                    AbstractNameQuery containerId = getResourceContainerId(name, j2eeType, null, gerResourceRef);

                    try {
                        localConfiguration.findGBean(containerId);
                    } catch (GBeanNotFoundException e) {
                        throw new UnresolvedReferenceException("Resource", false, containerId.toString(), localConfiguration.getId().toString());
                    }

                    Reference ref = new ResourceReference(localConfiguration.getId(), containerId, iface);
                    componentContext.put(ENV + name, ref);
                } catch (UnresolvedReferenceException e) {

                    StringBuffer errorMessage = new StringBuffer("Unable to resolve resource reference '");
                    errorMessage.append(name);
                    errorMessage.append("' (");
                    if (e.isMultiple()) {
                        errorMessage.append("Found multiple matching resources.  Try being more specific in a resource-ref mapping in your Geronimo deployment plan.");
                    } else if (gerResourceRef == null) {
                        errorMessage.append("Could not auto-map to resource.  Try adding a resource-ref mapping to your Geronimo deployment plan.");
                    } else if (gerResourceRef.isSetResourceLink()) {
                        errorMessage.append("Could not find resource '");
                        errorMessage.append(gerResourceRef.getResourceLink());
                        errorMessage.append("'.  Perhaps it has not yet been configured, or your application does not have a dependency declared for that resource module?");
                    } else {
                        errorMessage.append("Could not find the resource specified in your Geronimo deployment plan:");
                        errorMessage.append(gerResourceRef.getPattern());
                    }
                    errorMessage.append(")");

                    throw new DeploymentException(errorMessage.toString());
                }
            }
        }

    }

    private Map mapResourceRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerResourceRefType ref = (GerResourceRefType) refs[i].copy().changeType(GerResourceRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    private AbstractNameQuery getResourceContainerId(String name, String type, URI moduleURI, GerResourceRefType gerResourceRef) {
        AbstractNameQuery containerId;
        String module = moduleURI == null ? null : moduleURI.toString();
        if (gerResourceRef == null) {
            containerId = buildAbstractNameQuery(null, module, name, type, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else if (gerResourceRef.isSetResourceLink()) {
            containerId = buildAbstractNameQuery(null, module, gerResourceRef.getResourceLink().trim(), type, NameFactory.RESOURCE_ADAPTER_MODULE);
        } else {
            //construct name from components
            GerPatternType patternType = gerResourceRef.getPattern();
            containerId = buildAbstractNameQuery(patternType, type, NameFactory.RESOURCE_ADAPTER_MODULE, null);
        }
        return containerId;
    }

    public QNameSet getSpecQNameSet() {
        return RESOURCE_REF_QNAME_SET;
    }

    public QNameSet getPlanQNameSet() {
        return GER_RESOURCE_REF_QNAME_SET;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(ResourceRefBuilder.class, NameFactory.MODULE_BUILDER);

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
