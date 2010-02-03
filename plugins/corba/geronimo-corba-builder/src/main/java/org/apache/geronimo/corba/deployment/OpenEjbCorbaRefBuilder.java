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

package org.apache.geronimo.corba.deployment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.deployment.EjbRefBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.javaee6.EjbRefType;
import org.apache.geronimo.corba.proxy.CORBAProxyReference;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * Installs ejb refs that use corba transport into jndi context.
 * Such ejb refs are determined by the nscorbaloc element in the openejb ejb plan.
 *  
 * @version $Revision$ $Date$
 */
public class OpenEjbCorbaRefBuilder extends EjbRefBuilder {

    private static final QName GER_EJB_REF_QNAME = GerEjbRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_REF_QNAME_SET = QNameSet.singleton(GER_EJB_REF_QNAME);
    private static final NamespaceElementConverter OPENEJB_CONVERTER = new NamespaceElementConverter(GER_EJB_REF_QNAME.getNamespaceURI());

    private static final QName GER_NS_CORBA_LOC_QNAME = new QName(GER_EJB_REF_QNAME.getNamespaceURI(), "ns-corbaloc");
    private static final QNameSet GER_NS_CORBA_LOC_QNAME_SET = QNameSet.singleton(GER_NS_CORBA_LOC_QNAME);

    private final QNameSet ejbRefQNameSet;

    public OpenEjbCorbaRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) throws URISyntaxException {
        super(defaultEnvironment, new String[0], null, -1);
        ejbRefQNameSet = buildQNameSet(eeNamespaces, "ejb-ref");
    }

    @Override
    public QNameSet getSpecQNameSet() {
        return ejbRefQNameSet;
    }

    @Override
    public QNameSet getPlanQNameSet() {
        return GER_NS_CORBA_LOC_QNAME_SET;
    }


    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan)  {
//        return hasCssRefs(plan);
        return true;
    }

//    static boolean hasCssRefs(XmlObject plan) throws DeploymentException {
//        XmlObject[] refs = plan == null ? NO_REFS : convert(plan.selectChildren(GER_EJB_REF_QNAME_SET), OPENEJB_CONVERTER, GerEjbRefType.type);
//        for (int i = 0; i < refs.length; i++) {
//            GerEjbRefType ref = (GerEjbRefType) refs[i];
//            if (ref.isSetNsCorbaloc()) {
//                return true;
//            }
//        }
//        return false;
//    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] ejbRefsUntyped = convert(specDD.selectChildren(ejbRefQNameSet), JEE_CONVERTER, EjbRefType.type);
        XmlObject[] gerEjbRefsUntyped = plan == null ? NO_REFS : convert(plan.selectChildren(GER_EJB_REF_QNAME_SET), OPENEJB_CONVERTER, GerEjbRefType.type);
        Map ejbRefMap = mapEjbRefs(gerEjbRefsUntyped);
        ClassLoader cl = module.getEarContext().getClassLoader();

        for (XmlObject anEjbRefsUntyped : ejbRefsUntyped) {
            EjbRefType ejbRef = (EjbRefType) anEjbRefsUntyped;

            String ejbRefName = getStringValue(ejbRef.getEjbRefName());
            addInjections(ejbRefName, ejbRef.getInjectionTargetArray(), componentContext);
            GerEjbRefType remoteRef = (GerEjbRefType) ejbRefMap.get(ejbRefName);

            Reference ejbReference = addEJBRef(module, ejbRef, remoteRef, cl);
            if (ejbReference != null) {
                getJndiContextMap(componentContext).put(ENV + ejbRefName, ejbReference);
            }
        }
    }

    private Reference addEJBRef(Module module, EjbRefType ejbRef, GerEjbRefType remoteRef, ClassLoader cl) throws DeploymentException {
        Reference ejbReference = null;
        if (remoteRef != null && remoteRef.isSetNsCorbaloc()) {
            String refName = getStringValue(ejbRef.getEjbRefName());
            String home = getStringValue(ejbRef.getHome());
            String remote = getStringValue(ejbRef.getRemote());

            verifyInterfaces(refName, module.getModuleURI(), cl, remote, home);

            try {
                // create the cssBean query
                AbstractNameQuery cssBean;
                if (remoteRef.isSetCssLink()) {
                    String cssLink = remoteRef.getCssLink().trim();
                    cssBean = buildAbstractNameQuery(null, null, cssLink, NameFactory.CORBA_CSS, NameFactory.EJB_MODULE);
                } else {
                    GerPatternType css = remoteRef.getCss();
                    cssBean = buildAbstractNameQuery(css, NameFactory.CORBA_CSS, NameFactory.EJB_MODULE, null);
                }

                // verify the cssBean query is valid
                try {
                    module.getEarContext().findGBean(cssBean);
                } catch (GBeanNotFoundException e) {
                    throw new DeploymentException("Could not find css bean matching " + cssBean + " from configuration " + module.getConfigId(), e);
                }

                // create ref
                ejbReference = new CORBAProxyReference(module.getConfigId(), cssBean, new URI(remoteRef.getNsCorbaloc().trim()), remoteRef.getName().trim(), home);
            } catch (URISyntaxException e) {
                throw new DeploymentException("Could not construct CORBA NameServer URI: " + remoteRef.getNsCorbaloc(), e);
            }
        }
        return ejbReference;
    }

    private void verifyInterfaces(String refName, URI moduleURI, ClassLoader cl, String remote, String home) throws DeploymentException {
        try {
            assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'remote' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage(), e);
        }
        try {
            assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'home' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage(), e);
        }
    }

    private static Map mapEjbRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbRefType ref = (GerEjbRefType) refs[i];
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }


    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEjbCorbaRefBuilder.class, NameFactory.MODULE_BUILDER); //TODO decide what type this should be
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[]{"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
