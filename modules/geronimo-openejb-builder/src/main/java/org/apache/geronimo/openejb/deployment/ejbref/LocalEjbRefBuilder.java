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
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.javaee.EjbLocalRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Revision: 470469 $ $Date: 2006-11-02 10:43:34 -0800 (Thu, 02 Nov 2006) $
 */
public class LocalEjbRefBuilder extends AbstractEjbRefBuilder {
    private static final QName GER_EJB_LOCAL_REF_QNAME = GerEjbLocalRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_LOCAL_REF_QNAME_SET = QNameSet.singleton(GER_EJB_LOCAL_REF_QNAME);

    private final QNameSet ejbLocalRefQNameSet;

    public LocalEjbRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        ejbLocalRefQNameSet = buildQNameSet(eeNamespaces, "ejb-local-ref");
    }

    public QNameSet getSpecQNameSet() {
        return ejbLocalRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_EJB_LOCAL_REF_QNAME_SET;
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return specDD.selectChildren(ejbLocalRefQNameSet).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        List<EjbLocalRefType> ejbLocalRefs = convert(specDD.selectChildren(ejbLocalRefQNameSet), J2EE_CONVERTER, EjbLocalRefType.class, EjbLocalRefType.type);
        XmlObject[] gerEjbLocalRefsUntyped = plan == null ? NO_REFS : plan.selectChildren(GER_EJB_LOCAL_REF_QNAME_SET);
        Map<String, GerEjbLocalRefType> ejbLocalRefMap = mapEjbLocalRefs(gerEjbLocalRefsUntyped);
        ClassLoader cl = module.getEarContext().getClassLoader();

        for (EjbLocalRefType ejbLocalRef : ejbLocalRefs) {
            String ejbRefName = getStringValue(ejbLocalRef.getEjbRefName());
            GerEjbLocalRefType localRef = ejbLocalRefMap.get(ejbRefName);

            Reference ejbReference = createEjbLocalRef(remoteConfiguration, module.getModuleURI(), ejbLocalRef, localRef, cl);
            if (ejbReference != null) {
                //noinspection unchecked
                getJndiContextMap(componentContext).put(ENV + ejbRefName, ejbReference);
            }
        }
    }

    private Reference createEjbLocalRef(Configuration ejbContext, URI moduleURI, EjbLocalRefType ejbLocalRef, GerEjbLocalRefType localRef, ClassLoader cl) throws DeploymentException {
        String refName = getStringValue(ejbLocalRef.getEjbRefName());

        String local = getStringValue(ejbLocalRef.getLocal());
        try {
            assureEJBLocalObjectInterface(local, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'local' element for EJB Local Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        String localHome = getStringValue(ejbLocalRef.getLocalHome());
        try {
            assureEJBLocalHomeInterface(localHome, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'local-home' element for EJB Local Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        boolean isSession = "Session".equals(getStringValue(ejbLocalRef.getEjbRefType()));

        String ejbLink = null;
        if (localRef != null && localRef.isSetEjbLink()) {
            ejbLink = localRef.getEjbLink();
        } else if (ejbLocalRef.isSetEjbLink()) {
            ejbLink = getStringValue(ejbLocalRef.getEjbLink());
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
                ejbLink = bits[1];
            }
        } else if (localRef != null) {
            GerPatternType patternType = localRef.getPattern();
            containerQuery = buildAbstractNameQuery(patternType, null, NameFactory.EJB_MODULE, null);
        }
        return createEjbRef(refName, ejbContext, ejbLink, requiredModule, optionalModule, containerQuery, isSession, localHome, local, false);
    }

    private static Map<String, GerEjbLocalRefType> mapEjbLocalRefs(XmlObject[] refs) {
        Map<String, GerEjbLocalRefType> refMap = new HashMap<String, GerEjbLocalRefType>();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbLocalRefType ref = (GerEjbLocalRefType) refs[i].copy().changeType(GerEjbLocalRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    public static Class assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        return assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    public static Class assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        return assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(LocalEjbRefBuilder.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
