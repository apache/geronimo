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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.corba.proxy.CORBAProxyReference;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.j2ee.annotation.ReferenceType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.openejb.deployment.EjbRefBuilder;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.openejb.jee.EjbRef;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.osgi.framework.Bundle;

/**
 * Installs ejb refs that use corba transport into jndi context.
 * Such ejb refs are determined by the nscorbaloc element in the openejb ejb plan.
 *  
 * @version $Revision$ $Date$
 */
@GBean(j2eeType = NameFactory.MODULE_BUILDER)
public class OpenEjbCorbaRefBuilder extends EjbRefBuilder {

    private static final QName GER_EJB_REF_QNAME = GerEjbRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_REF_QNAME_SET = QNameSet.singleton(GER_EJB_REF_QNAME);
    private static final NamespaceElementConverter OPENEJB_CONVERTER = new NamespaceElementConverter(GER_EJB_REF_QNAME.getNamespaceURI());

    private static final QName GER_NS_CORBA_LOC_QNAME = new QName(GER_EJB_REF_QNAME.getNamespaceURI(), "ns-corbaloc");
    private static final QNameSet GER_NS_CORBA_LOC_QNAME_SET = QNameSet.singleton(GER_NS_CORBA_LOC_QNAME);

    private final QNameSet ejbRefQNameSet;

    public OpenEjbCorbaRefBuilder(@ParamAttribute(name = "defaultEnvironment")Environment defaultEnvironment,
                                  @ParamAttribute(name = "eeNamespaces")String[] eeNamespaces) throws URISyntaxException {
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


    protected boolean willMergeEnvironment(JndiConsumer specDD, XmlObject plan)  {
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

    public void buildNaming(JndiConsumer specDD, XmlObject plan, Module module, Map<EARContext.Key, Object> sharedContext) throws DeploymentException {
        Collection<EjbRef> ejbRefsUntyped = specDD.getEjbRef();
        XmlObject[] gerEjbRefsUntyped = plan == null ? NO_REFS : convert(plan.selectChildren(GER_EJB_REF_QNAME_SET), OPENEJB_CONVERTER, GerEjbRefType.type);
        Map ejbRefMap = mapEjbRefs(gerEjbRefsUntyped);
        Bundle bundle = module.getEarContext().getDeploymentBundle();

        for (EjbRef ejbRef : ejbRefsUntyped) {
            String ejbRefName = getStringValue(ejbRef.getKey());
            GerEjbRefType remoteRef = (GerEjbRefType) ejbRefMap.get(ejbRefName);

            Reference ejbReference = addEJBRef(module, ejbRef, remoteRef, bundle);
            if (ejbReference != null) {
                put(ejbRefName, ejbReference, ReferenceType.EJB, module.getJndiContext(), ejbRef.getInjectionTarget(), sharedContext);
            }
        }
    }

    private Reference addEJBRef(Module module, EjbRef ejbRef, GerEjbRefType remoteRef, Bundle bundle) throws DeploymentException {
        Reference ejbReference = null;
        if (remoteRef != null && remoteRef.isSetNsCorbaloc()) {
            String refName = getStringValue(ejbRef.getEjbRefName());
            String home = getStringValue(ejbRef.getHome());
            String remote = getStringValue(ejbRef.getRemote());

            verifyInterfaces(refName, module.getModuleURI(), bundle, remote, home);

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

    private void verifyInterfaces(String refName, URI moduleURI, Bundle bundle, String remote, String home) throws DeploymentException {
        try {
            assureInterface(remote, "javax.ejb.EJBObject", "Remote", bundle);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'remote' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage(), e);
        }
        try {
            assureInterface(home, "javax.ejb.EJBHome", "Home", bundle);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'home' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage(), e);
        }
    }

    private static Map mapEjbRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbRefType ref = (GerEjbRefType) refs[i];
                refMap.put(getJndiName(ref.getRefName().trim()), ref);
            }
        }
        return refMap;
    }
    
    @Override
    public int getPriority() {
        return 55;
    }    

}
