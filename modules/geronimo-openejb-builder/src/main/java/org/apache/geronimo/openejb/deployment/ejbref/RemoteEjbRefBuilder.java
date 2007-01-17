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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.javaee.EjbRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 475950 $ $Date: 2006-11-16 14:18:14 -0800 (Thu, 16 Nov 2006) $
 */
public class RemoteEjbRefBuilder extends AbstractEjbRefBuilder {
    private static final QName GER_EJB_REF_QNAME = GerEjbRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_REF_QNAME_SET = QNameSet.singleton(GER_EJB_REF_QNAME);

    private final QNameSet ejbRefQNameSet;

    public RemoteEjbRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        ejbRefQNameSet = buildQNameSet(eeNamespaces, "ejb-ref");
    }

    public QNameSet getSpecQNameSet() {
        return ejbRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_EJB_REF_QNAME_SET;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(ejbRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        List<EjbRefType> ejbRefs = convert(specDD.selectChildren(ejbRefQNameSet), J2EE_CONVERTER, EjbRefType.class, EjbRefType.type);
        XmlObject[] gerEjbRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_EJB_REF_QNAME_SET);
        Map<String, GerEjbRefType> ejbRefMap = mapEjbRefs(gerEjbRefsUntyped);
        ClassLoader cl = module.getEarContext().getClassLoader();

        for (EjbRefType ejbRef : ejbRefs) {
            String ejbRefName = getStringValue(ejbRef.getEjbRefName());
            GerEjbRefType remoteRef = ejbRefMap.get(ejbRefName);

            Reference ejbReference = createEjbRef(remoteConfiguration, module.getModuleURI(), ejbRef, remoteRef, cl);
            if (ejbReference != null) {
                //noinspection unchecked
                getJndiContextMap(componentContext).put(ENV + ejbRefName, ejbReference);
            }
        }
    }

    private Reference createEjbRef(Configuration ejbContext, URI moduleURI, EjbRefType ejbRef, GerEjbRefType remoteRef, ClassLoader cl) throws DeploymentException {
        String refName = getStringValue(ejbRef.getEjbRefName());

        String remote = getStringValue(ejbRef.getRemote());
        try {
            assureEJBObjectInterface(remote, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'remote' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        String home = getStringValue(ejbRef.getHome());
        try {
            assureEJBHomeInterface(home, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'home' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        boolean isSession = "Session".equals(getStringValue(ejbRef.getEjbRefType()));

        // MEJB
        if (isSession && remote.equals("javax.management.j2ee.Management") && home.equals("javax.management.j2ee.ManagementHome")) {
            AbstractNameQuery query = new AbstractNameQuery(null, Collections.singletonMap("name", "ejb/mgmt/MEJB"));
            return createEjbRef(null, ejbContext, null, null, null, query, isSession, home, remote, true);
        }

        // corba refs are handled by another builder
        if (remoteRef != null && remoteRef.isSetNsCorbaloc()) {
            return null;
        }

        String ejbLink = null;
        if (remoteRef != null && remoteRef.isSetEjbLink()) {
            ejbLink = remoteRef.getEjbLink();
        } else if (ejbRef.isSetEjbLink()) {
            ejbLink = getStringValue(ejbRef.getEjbLink());
        }

        String optionalModule;
        if (moduleURI == null) {
            optionalModule = null;
        } else {
            optionalModule = moduleURI.toString();
        }

        String requiredModule = null;
        AbstractNameQuery containerQuery = null;
        if (ejbLink != null) {
            String[] bits = ejbLink.split("#");
            if (bits.length == 2) {
                //look only in specified module.
                requiredModule = bits[0];
                if (moduleURI != null) {
                    requiredModule = moduleURI.resolve(requiredModule).getPath();
                }
                ejbLink = bits[1];
            }
        } else if (remoteRef != null) {
            GerPatternType patternType = remoteRef.getPattern();
            containerQuery = buildAbstractNameQuery(patternType, null, NameFactory.EJB_MODULE, null);
        }
        return createEjbRef(refName, ejbContext, ejbLink, requiredModule, optionalModule, containerQuery, isSession, home, remote, true);
    }

    private static Map<String, GerEjbRefType> mapEjbRefs(XmlObject[] refs) {
        Map<String, GerEjbRefType> refMap = new HashMap<String, GerEjbRefType>();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbRefType ref = (GerEjbRefType) refs[i].copy().changeType(GerEjbRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    public static Class assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        return assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    public static Class assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        return assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(RemoteEjbRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
